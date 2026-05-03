package com.example.online_bank.config.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class ServicesLoggingAspect {

    @Pointcut(value = "execution(* com.example.online_bank.service.*.*(..))")
    public void pointCut() {
    }

    @Before("pointCut()")
    public void logBefore(JoinPoint joinPoint) {
           String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] methodArgs = joinPoint.getArgs();
        log.info("Метод {} с аргументами {}", className + "." + methodName, methodArgs);
    }
}
