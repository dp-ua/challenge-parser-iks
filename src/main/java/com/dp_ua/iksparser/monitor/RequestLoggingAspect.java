package com.dp_ua.iksparser.monitor;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class RequestLoggingAspect {

    @Around("@annotation(logRequestDetails)")
    public Object logRequest(ProceedingJoinPoint joinPoint, LogRequestDetails logRequestDetails) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        StringBuilder logMessage = new StringBuilder();
        logMessage.append("URI: ").append(request.getRequestURI());
        logMessage.append(", Request from IP: ").append(request.getRemoteAddr());
        logMessage.append(", User-Agent: ").append(request.getHeader("User-Agent"));

        // Логирование указанных параметров
        String[] parametersToLog = logRequestDetails.parameters();
        for (String param : parametersToLog) {
            String paramValue = request.getParameter(param);
            if (paramValue != null) {
                logMessage.append(", ").append(param).append(": ").append(paramValue);
            }
        }

        log.info(logMessage.toString());

        return joinPoint.proceed();
    }
}
