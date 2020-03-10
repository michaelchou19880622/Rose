package com.bcs.web.aop;

import com.bcs.core.taishin.akka.service.TaishinAkkaService;
import com.bcs.core.taishin.api.model.LogApiModel;
import com.bcs.core.utils.IpUtil;
import com.bcs.core.web.security.CustomUser;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
public class LogAspect {
    private static Logger logger = Logger.getLogger(LogAspect.class);

    private String READ = "((.*)get(.*)|(.*)count(.*)|(.*)page(.*))";
    private String CREATE = "((.*)create(.*)|(.*)save(.*)|(.*)redisgn(.*)|(.*)upload(.*)|(.*)setting(.*)|(.*)send(.*))";
    private String UPDATE = "((.*)update(.*))";
    private String DELETE = "((.*)delete(.*))";
    private String EXPORT = "((.*)export(.*))";
    private String REPORT = "((.*)report(.*))";

    @Autowired
    private TaishinAkkaService taishinAkkaService;

    /**
     * Controller層切入點
     */
    @Pointcut("@annotation(com.bcs.web.aop.ControllerLog)")
    public void controllerAspect() {
    }

    @After("controllerAspect()")
    public void doBefore(JoinPoint joinPoint) {
        try {
            logger.info("=====前置通知開始=====");

            String methodName = joinPoint.getSignature().getName();
            String description = getControllerMethodDescription(joinPoint);
            String functionType = "";
            List<Object> objectListData = new ArrayList<>();
            Object[] args = joinPoint.getArgs();

            String ip = getIp(args);
            String accountAndMid = getAccountAndMid(args);

            for (int index = 3; index < args.length; index++) {
                Object data = args[index];

                if (data instanceof MultipartFile) {
                    MultipartFile filePart = (MultipartFile) data;
                    String fileName = filePart.getOriginalFilename();
                    objectListData.add(fileName);
                } else {
                    objectListData.add(data);
                }
            }

            if (methodName.toLowerCase().matches(READ)) {
                functionType = LogApiModel.READ;
            } else if (methodName.toLowerCase().matches(CREATE)) {
                functionType = LogApiModel.CREATE;
            } else if (methodName.toLowerCase().matches(UPDATE)) {
                functionType = LogApiModel.UPDATE;
            } else if (methodName.toLowerCase().matches(DELETE)) {
                functionType = LogApiModel.DELETE;
            } else if (methodName.toLowerCase().matches(EXPORT)) {
                functionType = LogApiModel.EXPORT;
            } else if (methodName.toLowerCase().matches(REPORT)) {
                functionType = LogApiModel.REPORT;
            }

            if (functionType.isEmpty()) {
                functionType = LogApiModel.READ;
            }

            LogApiModel logApiModel = new LogApiModel.LogApiModelBuilder().functionType(functionType)
                    .functionName(description).queryString(methodName).data(objectListData).clientIp(ip)
                    .userId(accountAndMid).functionStatus(LogApiModel.SUCCESS).build();

//            if (accountAndMid != null && ip != null) {
//                taishinAkkaService.excuteLogApi(logApiModel);
//            }

            logger.info("=====前置通知结束=====");
        } catch (Exception e) {
            logger.error("==前置通知異常==" + e);
        }
    }

    private static String getControllerMethodDescription(JoinPoint joinPoint) throws Exception {
        String targetName = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] arguments = joinPoint.getArgs();
        Class targetClass = Class.forName(targetName);
        Method[] methods = targetClass.getMethods();
        String description = "";
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class[] clazzs = method.getParameterTypes();
                if (clazzs.length == arguments.length) {
                    description = method.getAnnotation(ControllerLog.class).description();
                    break;
                }
            }
        }
        return description;
    }

    private static String getIp(Object[] args) {
        HttpServletRequest request;
        request = (args[0] instanceof HttpServletRequest) ? (HttpServletRequest) args[0] : null;

        // double check
        if (request == null) {
            for (Object arg : args) {
                if (arg instanceof CustomUser) {
                    request = (HttpServletRequest) arg;
                    break;
                }
            }
        }
        return IpUtil.getIpAddress(request);
    }

    private static String getAccountAndMid(Object[] args) {
        CustomUser customUser;
        customUser = (args[2] instanceof CustomUser) ? (CustomUser) args[2] : null;

        // double check
        if (customUser == null) {
            for (Object arg : args) {
                if (arg instanceof CustomUser) {
                    customUser = (CustomUser) arg;
                    break;
                }
            }
        }

        return (customUser != null) ? customUser.getAccount() + "," + (customUser.getMid() != null ? customUser.getMid() : "MidNull") : null;
    }

}
