package dyamo.narek.syntechnica.global.errors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Aspect
@Component
public class DefaultExceptionHandlerAspect {

	@Around("isController() && @annotation(defaultExceptionHandler)")
	public Object handleDefaultExceptions(ProceedingJoinPoint joinPoint,
										  DefaultExceptionHandler defaultExceptionHandler) throws Throwable {
		try {
			return joinPoint.proceed();
		} catch (Throwable thrown) {
			Class<? extends Exception>[] exceptionClasses = defaultExceptionHandler.exceptions();

			boolean thrownMustBeHandled = exceptionClasses.length == 0 ||
					Arrays.stream(exceptionClasses).anyMatch(exceptionClass -> exceptionClass.isInstance(thrown));


			if (thrownMustBeHandled) {
				throw new DefaultHandledException(defaultExceptionHandler.responseStatus(), thrown);
			}

			throw thrown;
		}
	}

	@Around("isController() && @annotation(defaultExceptionHandlerGroup)")
	public Object handleDefaultExceptions(ProceedingJoinPoint joinPoint,
										  DefaultExceptionHandler.Group defaultExceptionHandlerGroup
	) throws Throwable {
		try {
			return joinPoint.proceed();
		} catch (Throwable thrown) {
			DefaultExceptionHandler[] defaultExceptionHandlers = defaultExceptionHandlerGroup.value();

			Optional<HttpStatus> responseStatus = Arrays.stream(defaultExceptionHandlers)
					.filter(handler ->
							Arrays.stream(handler.exceptions())
									.anyMatch(exceptionClass -> exceptionClass.isInstance(thrown))
					)
					.map(DefaultExceptionHandler::responseStatus)
					.findFirst()
					.or(() ->
							Arrays.stream(defaultExceptionHandlers)
									.filter(handler -> handler.exceptions().length == 0)
									.map(DefaultExceptionHandler::responseStatus)
									.findFirst()
					);


			if (responseStatus.isPresent()) {
				throw new DefaultHandledException(responseStatus.get(), thrown);
			}

			throw thrown;
		}
	}


	@Pointcut("within(@org.springframework.stereotype.Controller *) || " +
			"within(@(@org.springframework.stereotype.Controller *) *)")
	private void isController() {}

}
