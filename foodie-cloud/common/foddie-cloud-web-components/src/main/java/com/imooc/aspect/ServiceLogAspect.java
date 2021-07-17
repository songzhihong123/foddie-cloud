package com.imooc.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceLogAspect {

    public static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    /**
     * AOP 通知
     * 1、前置通知：在目标方法调用之前执行
     * 2、后置通知：在目标方法正常调用之后执行
     * 3、环绕通知：在目标方法调用之前和之后执行
     * 4、返回后通知：在目标发放调用之后执行
     * 5、异常通知：目标方法调用过程当中发生异常通知
     */


    /**
     * 切面表达式
     * execution 代表所要执行的表达式主体
     * 第一处 * 代表方法返回值类型 * 代表所有的类型
     * 第二处 包名 【com.imooc.service.impl】 代表AOP监控的包
     * 第三处 .. 代表该包极其子包下的所有类方法
     * 第四处 * 代表类名， * 代表所有的类
     * 第五处 *(..) *代表类中的方法名，(..)表示方法中的任何参数
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("execution(* com.imooc..*.service.impl..*.*(..))")
    public Object recordTimeLog(ProceedingJoinPoint joinPoint) throws Throwable{
        logger.info("====== 开始执行 {}.{} =======",joinPoint.getTarget().getClass(),joinPoint.getSignature().getName());

        //记录开始时间
        long begin = System.currentTimeMillis();
        //执行目标service
        Object result = joinPoint.proceed();
        //记录开始时间
        long end = System.currentTimeMillis();
        long takeTime = end - begin;
        if (takeTime > 3000){
            logger.error("====== 执行结束，耗时:{}毫秒 ======",takeTime);
        }else if(takeTime > 2000){
            logger.warn("====== 执行结束，耗时:{}毫秒 ======",takeTime);
        }else{
            logger.info("====== 执行结束，耗时:{}毫秒 ======",takeTime);
        }
        return result;
    }





}
