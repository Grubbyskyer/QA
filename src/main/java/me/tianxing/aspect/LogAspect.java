package me.tianxing.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by TX on 2016/7/20.
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Before("execution(* me.tianxing.controller.*Controller.*(..))") //第一个*是返回值 最后那个是方法名
    public void beforeMethod(JoinPoint joinPoint) { // joinPoint 是一个切点 看是在哪切的 也就是调用了哪个方法
        StringBuilder sb = new StringBuilder();
        for (Object arg : joinPoint.getArgs()) {
            sb.append(arg.toString() + "|");
        }
        logger.info("before method: " + sb.toString());
    }

    @After("execution(* me.tianxing.controller.IndexController.*(..))")
    public void afterMethod() {
        logger.info("after method");
    }

}
