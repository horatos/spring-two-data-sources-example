package com.example;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ShardContextAspect {

    @Around("@annotation(com.example.UseDataSourceOne) || @within(com.example.UseDataSourceOne)")
    public Object useOne(ProceedingJoinPoint pjp) throws Throwable {
        ShardContext.setShard("ONE");
        try {
            return pjp.proceed();
        } finally {
            ShardContext.clear();
        }
    }

    @Around("@annotation(com.example.UseDataSourceTwo) || @within(com.example.UseDataSourceTwo)")
    public Object useTwo(ProceedingJoinPoint pjp) throws Throwable {
        ShardContext.setShard("TWO");
        try {
            return pjp.proceed();
        } finally {
            ShardContext.clear();
        }
    }
}
