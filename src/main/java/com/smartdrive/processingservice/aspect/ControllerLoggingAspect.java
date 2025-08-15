package com.smartdrive.processingservice.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ControllerLoggingAspect {

    private final ObjectMapper objectMapper;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void anyRestController() {}

    @Before("anyRestController()")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." +
                joinPoint.getSignature().getName();

        String argsJson = safeToJson(joinPoint.getArgs());
        log.info("@Before: {} args: {}", methodName, argsJson);
    }

    @AfterReturning(pointcut = "anyRestController()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." +
                joinPoint.getSignature().getName();

        Object response = (result instanceof ResponseEntity<?> re) ? re : result;
        String responseJson = safeToJson(response);

        log.info("@AfterReturning normally: {} With response: {}", methodName, responseJson);
    }

    @AfterThrowing(pointcut = "anyRestController()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." +
                joinPoint.getSignature().getName();

        log.error("@AfterThrowing: {} threw exception: {}", methodName, ex.getMessage(), ex);
    }

    private String safeToJson(Object obj) {
        try {
            if (obj == null) return "null";
            if (obj.getClass().isArray()) return objectMapper.writeValueAsString(Arrays.asList((Object[]) obj));
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}
