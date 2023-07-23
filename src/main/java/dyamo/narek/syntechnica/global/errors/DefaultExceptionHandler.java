package dyamo.narek.syntechnica.global.errors;

import org.springframework.http.HttpStatus;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DefaultExceptionHandler.Group.class)
public @interface DefaultExceptionHandler {

	Class<? extends Exception>[] exceptions() default {};

	HttpStatus responseStatus() default HttpStatus.BAD_REQUEST;


	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface Group {

		DefaultExceptionHandler[] value();

	}

}
