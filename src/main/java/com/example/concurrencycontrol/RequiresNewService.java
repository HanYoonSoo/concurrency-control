package com.example.concurrencycontrol;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequiresNewService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object proceed(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
        return proceedingJoinPoint.proceed();
    }
}
