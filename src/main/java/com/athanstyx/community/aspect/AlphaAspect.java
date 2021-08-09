package com.athanstyx.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {

    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")//包括这些切点
    public void pointcut() {

    }

    @Before("pointcut()") //针对那些切点
    public void before() {

        System.out.println("before");
    }

    //执行完毕后
    @After("pointcut()") //针对那些切点
    public void after() {
        System.out.println("after");
    }

    //返回值的时候
    @AfterReturning("pointcut()") //针对那些切点
    public void afterReturning() {
        System.out.println("afterReturning");
    }

    //跑出异常的时候
    @AfterThrowing("pointcut()") //针对那些切点
    public void afterThrowing() {
        System.out.println("afterThrowing");
    }

    //前后都织入
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("around before");
        Object object = joinPoint.proceed();//调用目标组件的方法可能有返回值
        System.out.println("around after");
        return object;
    }




}
