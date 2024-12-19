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

private val logger: Logger = LoggerFactory.getLogger(LoggingAspect::class.java)
@Aspect
@Component
class LoggingAspect {

    @Before("execution(* hampusborg.bankapp.*.*(..))")
    fun logBeforeMethodExecution(joinPoint: JoinPoint) {
        val methodName = joinPoint.signature.name
        val arguments = joinPoint.args.joinToString(", ") { it?.toString() ?: "null" }
        logger.info("Method: $methodName with arguments: $arguments")
    }

    @After("execution(* hampusborg.bankapp.presentation.controller.*.*(..)) || execution(* hampusborg.bankapp.application.service.*.*(..))")
    fun logAfterMethodExecution(joinPoint: JoinPoint) {
        val methodName = joinPoint.signature.name
        logger.info("After executing method: $methodName")
    }

    @AfterThrowing(
        pointcut = "execution(* hampusborg.bankapp.presentation.controller.*.*(..)) || execution(* hampusborg.bankapp.application.service.*.*(..))",
        throwing = "exception"
    )
    fun logAfterMethodThrowsException(joinPoint: JoinPoint, exception: Throwable) {
        val methodName = joinPoint.signature.name
        logger.error("Method: $methodName threw an exception: ${exception.message}")
    }

    @AfterReturning(pointcut = "execution(* hampusborg.bankapp..*(..))", returning = "result")
    fun logAfterMethodReturn(joinPoint: JoinPoint, result: Any?) {
        if (result == null) {
            logger.warn("Method ${joinPoint.signature.name} returned null")
        } else {
            logger.info("Method ${joinPoint.signature.name} returned: $result")
        }
    }
}