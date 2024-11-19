package com.example.concurrencycontrol.aop;

import com.example.concurrencycontrol.service.RequiresNewService;
import com.example.concurrencycontrol.annotation.RedLock;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class RedLockAop {

    private final RedissonClient redissonClient;
    private final RequiresNewService requiresNewService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Around("@annotation(com.example.concurrencycontrol.annotation.RedLock)")
    public Object lock(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        RedLock redLock = signature.getMethod().getAnnotation(RedLock.class);

        RLock lock = redissonClient.getLock(getDynamicValue(signature.getParameterNames(), proceedingJoinPoint.getArgs(), redLock.key()).toString());
        try{
            if(lock.tryLock(redLock.waitTime(), redLock.leaseTime(), redLock.timeUnit())){
                return requiresNewService.proceed(proceedingJoinPoint);
            } else {
                throw new RuntimeException();
            }
        } catch (Exception e){
            throw e;
        } finally {
            if(lock.isLocked() && lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

//    @Around("@annotation(com.example.concurrencycontrol.annotation.RedLock)")
//    public Object lock(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
//        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
//        RedLock redLock = signature.getMethod().getAnnotation(RedLock.class);
//
//        RLock lock = redissonClient.getLock(getDynamicValue(signature.getParameterNames(), proceedingJoinPoint.getArgs(), redLock.key()).toString());
//        try{
//            if(lock.tryLock(redLock.waitTime(), redLock.leaseTime(), redLock.timeUnit())){
//                applicationEventPublisher.publishEvent(lock);
//                return proceedingJoinPoint.proceed();
//            } else{
//                return null;
//            }
//        } catch (Exception e){
//            throw e;
//        } finally {
////            if(lock.isLocked() && lock.isHeldByCurrentThread()){
////                applicationEventPublisher.publishEvent(lock);
////            }
//        }
//    }

    public static Object getDynamicValue(String[] parameterNames, Object[] args, String key){
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        for(int i = 0; i < parameterNames.length; i++){
            context.setVariable(parameterNames[i], args[i]);
        }

        return parser.parseExpression(key).getValue(context, Object.class);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void unLockEvent(RLock lock) {
        if(lock.isLocked() && lock.isHeldByCurrentThread()){
            lock.unlock();
        }
    }
}
