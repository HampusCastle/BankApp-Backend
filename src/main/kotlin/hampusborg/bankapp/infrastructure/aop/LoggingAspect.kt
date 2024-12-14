package hampusborg.bankapp.infrastructure.aop

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.AfterThrowing
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class LoggingAspect {

    private val logger: Logger = LoggerFactory.getLogger(LoggingAspect::class.java)

    @Before("execution(* hampusborg.bankapp.presentation.controller.*.*(..)) || execution(* hampusborg.bankapp.application.service.*.*(..))")
    fun logBeforeMethodExecution(joinPoint: JoinPoint) {
        val methodName = joinPoint.signature.name
        val args = joinPoint.args.joinToString(", ") { it.toString() }
        logger.info("Before executing method: $methodName with arguments: $args")
    }

    @After("execution(* hampusborg.bankapp.presentation.controller.*.*(..)) || execution(* hampusborg.bankapp.application.service.*.*(..))")
    fun logAfterMethodExecution(joinPoint: JoinPoint) {
        val methodName = joinPoint.signature.name
        logger.info("After executing method: $methodName")
    }

    @AfterThrowing(pointcut = "execution(* hampusborg.bankapp.presentation.controller.*.*(..)) || execution(* hampusborg.bankapp.application.service.*.*(..))", throwing = "exception")
    fun logAfterMethodThrowsException(joinPoint: JoinPoint, exception: Throwable) {
        val methodName = joinPoint.signature.name
        logger.error("Method: $methodName threw an exception: ${exception.message}")
    }

    @AfterReturning(pointcut = "execution(* hampusborg.bankapp.presentation.controller.*.*(..)) || execution(* hampusborg.bankapp.application.service.*.*(..))", returning = "result")
    fun logAfterMethodReturn(joinPoint: JoinPoint, result: Any) {
        val methodName = joinPoint.signature.name
        logger.info("Method: $methodName returned: $result")
    }
}