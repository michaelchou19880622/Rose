package com.bcs.core.aspect;


import com.bcs.core.aspect.annotation.WebServiceLog;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller參數攔截器
 * <p>
 * 於Controller前後攔截參數並做處理
 *
 * @author Alan
 * @see WebServiceLog
 */
@Slf4j
@Aspect
@Configuration
public class WebServiceAspect {

    /**
     * Use Annotation On Controller Method
     */
    private static final String userAnnotationPoint = "@annotation(com.bcs.core.aspect.annotation.WebServiceLog)";


    private Long startTime;

    @Pointcut(userAnnotationPoint)
    public void webLog() {
    }

    /**
     * Before Controller
     *
     * @param joinPoint joinPoint
     */
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) {
        MDC.put("RequestId", UUID.randomUUID().toString());
        log.info("Current Thread Name : {}", Thread.currentThread().getName());
        log.info("Current Thread ID   : {}", Thread.currentThread().getId());
        Thread.currentThread().setName("WebServiceLog-" + Thread.currentThread().getId());
        /*
         * Generate Request UUID
         * Log Pattern Add %X{RequestId}
         */

        startTime = System.currentTimeMillis();

        /* Request */
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.error("ServletRequestAttributes attributes = RequestContextHolder.getRequestAttributes() is Null!!");
            return;
        }
        HttpServletRequest request = attributes.getRequest();

        Map<String, Object> logMap = new LinkedHashMap<>(6);
        logMap.put("IP", IpUtil.getIpAddress(request));
        logMap.put("HTTP_METHOD", request.getMethod());
        logMap.put("URL", request.getRequestURL().toString());
        logMap.put("CLASS_METHOD", joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        logMap.put("ACTION", getAnnotationActionName(joinPoint));

        logMap.put("ARGS", joinPoint.getArgs());

        log.info("Request:\n{}", DataUtils.toPrettyJsonUseJackson(logMap));
    }

    /**
     * After Controller
     *
     * @param joinPoint joinPoint
     * @param ret       return
     */
    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(JoinPoint joinPoint, Object ret) {

        /* Request */
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.error("ServletRequestAttributes attributes = RequestContextHolder.getRequestAttributes() is Null!!");
            return;
        }
        HttpServletRequest request = attributes.getRequest();

        Map<String, Object> logMap = new LinkedHashMap<>(6);
        logMap.put("IP", IpUtil.getIpAddress(request));
        logMap.put("HTTP_METHOD", request.getMethod());
        logMap.put("URL", request.getRequestURL().toString());
        logMap.put("CLASS_METHOD", joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        logMap.put("ACTION", getAnnotationActionName(joinPoint));

        logMap.put("RESPONSE", ret);

        log.info("Response: Cast:{} ms \n{}", (System.currentTimeMillis() - startTime), DataUtils.toPrettyJsonUseJackson(logMap));

        /* Remove Request UUID  */
        MDC.clear();
    }

    /**
     * Get Annotation Action Name
     *
     * @param joinPoint joinPoint
     * @return Action Name
     */
    private String getAnnotationActionName(JoinPoint joinPoint) {
        try {
            /* Method Name From AOP */
            String methodName = joinPoint.getSignature().getName();
            /* Class From AOP */
            for (Method method : joinPoint.getTarget().getClass().getMethods()) {
                /* Class Method is Equal AOP Method */
                if (method.getName().equals(methodName)
                        && method.getParameterTypes().length == joinPoint.getArgs().length
                        && method.isAnnotationPresent(WebServiceLog.class)) {
                    /* 如果參數數量相同則取得Annotation */
                    return method.getAnnotation(WebServiceLog.class).action();
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
        return null;
    }
}
