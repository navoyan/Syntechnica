package dyamo.narek.syntechnica.global;

import dyamo.narek.syntechnica.global.errors.DefaultExceptionHandlerAspect;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ImportSecurityConfiguration
@Import({AopAutoConfiguration.class, DefaultExceptionHandlerAspect.class})
public @interface ImportControllerConfiguration {
}
