package com.example.online_bank.service;

import com.example.online_bank.domain.entity.Operation;
import com.example.online_bank.domain.entity.User;
import com.example.online_bank.enums.OperationType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openpdf.text.*;
import org.openpdf.text.Font;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PDFWriterService {
    private final UserService userService;
    private final OperationService operationService;

    private static final String REGULAR_FONT_PATH = "Roboto-Regular.ttf";
    private static final String BANK_NAME = "Online Bank";

    @SneakyThrows
    public byte[] write(Long operationId, UUID userUuid) {
        Operation operation = operationService.findById(operationId);
        log.info("Найденная сущность: {}", operation);
        User user = userService.findByUuid(userUuid).orElseThrow(EntityNotFoundException::new);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, byteArrayOutputStream);
            document.open();

            //Шрифты
            Font regular = new Font(getRegularFont(), 14);
            Font blueBold = new Font(getBoldFont(), 35, Font.BOLD, Color.BLUE);
            Font blueSeal = new Font(getBoldFont(), 24, Font.BOLD, Color.BLUE);
            Font blueRegular = new Font(getRegularFont(), 20, Font.NORMAL, Color.BLUE);

            //Параграфы
            Paragraph dummy = new Paragraph(" ");
            Paragraph headerParagraph = new Paragraph(20f);
            Paragraph dataParagraph = new Paragraph(22f);
            Paragraph sealParagraph = new Paragraph(20f);

            document.add(dummy);

            //Настройка параграфов
            headerParagraph.setAlignment(Element.ALIGN_CENTER);
            dataParagraph.setAlignment(Element.ALIGN_LEFT);
            sealParagraph.setAlignment(Element.ALIGN_CENTER);
            headerParagraph.setSpacingBefore(110f);
            headerParagraph.setSpacingAfter(30f);
            dataParagraph.setSpacingBefore(15f);
            dataParagraph.setSpacingAfter(30f);
            dataParagraph.setIndentationLeft(130f);

            //Фразы
            Phrase dataPhrase = new Phrase(16f);
            Phrase headerPhrase = new Phrase(16f);
            Phrase sealPhrase = new Phrase(20f);

            //создание заголовка для наименования банка
            Chunk headerChunk = new Chunk(BANK_NAME, blueBold);
            Chunk headerChunkSeal = new Chunk(BANK_NAME, blueSeal);
            headerPhrase.add(headerChunk);
            headerPhrase.add(Chunk.NEWLINE);

            Chunk operationNumberKey = new Chunk("ЧЕК ПО ОПЕРАЦИИ №", regular);
            Chunk operationNumberValue = new Chunk(operation.getId().toString(), regular);
            headerPhrase.add(operationNumberKey);
            headerPhrase.add(operationNumberValue);

            headerParagraph.add(headerPhrase);
            document.add(headerParagraph);

            //дата операции
            Chunk operationDateKey = new Chunk("ДАТА ОПЕРАЦИИ: ", regular);
            String operationDateValue = convertDateToString(operation.getCreatedAt());
            Chunk opDateValue = new Chunk(operationDateValue, regular);

            dataPhrase.add(operationDateKey);
            dataPhrase.add(opDateValue);
            dataPhrase.add(Chunk.NEWLINE);

            Chunk accNumberKey = new Chunk("НОМЕР СЧЁТА: ", regular);
            Chunk accNumberValue = new Chunk(operation.getAccount().getAccountNumber(), regular);

            dataPhrase.add(accNumberKey);
            dataPhrase.add(accNumberValue);
            dataPhrase.add(Chunk.NEWLINE);

            //СУММА ОПЕРАЦИИ: 10000 РУБ.
            Chunk amountKey = new Chunk("СУММА ОПЕРАЦИИ: ", regular);
            Chunk amountValue = new Chunk(operation.getAmount().toString(), regular);

            dataPhrase.add(amountKey);
            dataPhrase.add(amountValue);
            dataPhrase.add(Chunk.NEWLINE);
            // ТИП ОПЕРАЦИИ: Пополнение
            Chunk opTypeKey = new Chunk("ТИП ОПЕРАЦИИ: ", regular);
            String operationType = getOperationType(operation.getOperationType());
            Chunk opTypeValue = new Chunk(operationType, regular);

            dataPhrase.add(opTypeKey);
            dataPhrase.add(opTypeValue);
            dataPhrase.add(Chunk.NEWLINE);

            // ФИО:
            Chunk fullName = new Chunk("ФИО: ", regular);
            Chunk fullNameValue = new Chunk(getFullName(user), regular);

            dataPhrase.add(fullName);
            dataPhrase.add(fullNameValue);
            dataPhrase.add(Chunk.NEWLINE);
            dataParagraph.add(dataPhrase);
            document.add(dataParagraph);

            //Создание печати
            PdfPTable pdfPTable = new PdfPTable(1);
            pdfPTable.setWidthPercentage(50);
            pdfPTable.setSpacingBefore(20);

            sealPhrase.add(headerChunkSeal);
            sealPhrase.add(Chunk.NEWLINE);
            Chunk sealChunk2 = new Chunk("Операция выполнена", blueRegular);
            sealPhrase.add(sealChunk2);

            // sealParagraph.add(sealPhrase);

            PdfPCell cell = new PdfPCell(sealPhrase); //Добавляем параграф внутрь ячейки
            cell.setBorder(PdfCell.NO_BORDER);
            cell.setPadding(20);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);

            Color borderColor = Color.BLUE;
            float dashLength = 5f;
            float spaceLength = 3f;
            float thickness = 2f;

            cell.setCellEvent(new DottedBorderEvent(borderColor, dashLength, spaceLength, thickness));
            pdfPTable.addCell(cell);
            sealParagraph.add(pdfPTable);
            document.add(sealParagraph);

            document.close();

            byte[] byteArray = byteArrayOutputStream.toByteArray();
            log.info("byteArray - {}", byteArray);
            return byteArray;
        }
    }

    private String getOperationType(OperationType operationType) {
        switch (operationType) {
            case DEPOSIT -> {
                return "ПОПОЛНЕНИЕ";
            }
            case WITHDRAW -> {
                return "СПИСАНИЕ";
            }
            case BUY_CURRENCY -> {
                return "ПОКУПКА ВАЛЮТЫ МЕЖДУ СЧЕТАМИ";
            }
            case null, default -> {
                return "НЕИЗВЕСТНАЯ ОПЕРАЦИЯ";
            }
        }
    }

    private String getFullName(User user) {
        String patronymic;
        if (user.getPatronymic() == null || user.getPatronymic().isEmpty()) {
            patronymic = "";
            return String.format("%s %s %s", user.getSurname(), user.getName(), patronymic);
        } else {
            return String.format("%s %s %s", user.getSurname(), user.getName(), user.getPatronymic());
        }
    }

    private String convertDateToString(LocalDateTime date) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        return date.format(timeFormatter);
    }

    private BaseFont getRegularFont() throws IOException {
        return BaseFont.createFont(REGULAR_FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    }

    private BaseFont getBoldFont() throws IOException {
        return BaseFont.createFont(REGULAR_FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    }

    /**
     * Класс-помощник для рисования штриховой рамки вокруг ячейки
     */
    private record DottedBorderEvent(Color color, float dash, float space, float thickness) implements PdfPCellEvent {

        /**
         * @param cell     the cell
         * @param position the coordinates of the cell
         * @param canvases an array of <CODE>PdfContentByte</CODE>
         */
        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            //получаем холст
            PdfContentByte contentByte = canvases[PdfString.STRING];

            contentByte.saveState();
            contentByte.setColorStroke(color);
            contentByte.setLineWidth(thickness);

            //Паттерны штриха
            contentByte.setLineDash(dash, space, 0);

            //Рисуем прямоугольник по координатам нашей ячейки
            contentByte.rectangle(
                    position.getLeft(),
                    position.getBottom(),
                    position.getWidth(),
                    position.getHeight()
            );
            contentByte.stroke(); //применяем рисование линии
            contentByte.restoreState();
        }
    }

}
