package dyamo.narek.syntechnica.global.errors;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import static dyamo.narek.syntechnica.global.AopProxyBuilder.aopProxy;
import static org.assertj.core.api.Assertions.*;

class DefaultExceptionHandlerAspectTests {

	final TestController controllerProxy = aopProxy(new TestController(), DefaultExceptionHandlerAspect.class);

	final TestComponent componentProxy = aopProxy(new TestComponent(), DefaultExceptionHandlerAspect.class);


	@Test
	void handleDefaultExceptions_shouldWrapThrownException_whenThrownExceptionIsSpecified() {
		IllegalStateException originalException = new IllegalStateException();


		DefaultHandledException thrown = catchThrowableOfType(() -> {
			controllerProxy.endpointWithHandlerThrowing(originalException);
		}, DefaultHandledException.class);


		assertThat(thrown.getResponseStatus()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(thrown).hasCause(originalException);
	}

	@Test
	void handleDefaultExceptions_shouldWrapThrownException_whenNoExceptionIsSpecified() {
		NullPointerException originalException = new NullPointerException();


		DefaultHandledException thrown = catchThrowableOfType(() -> {
			controllerProxy.endpointWithGeneralHandlerThrowing(originalException);
		}, DefaultHandledException.class);


		assertThat(thrown.getResponseStatus()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(thrown).hasCause(originalException);
	}

	@Test
	void handleDefaultExceptions_shouldDoNothing_whenThrownExceptionIsNotSpecified() {
		NullPointerException originalException = new NullPointerException();


		Exception thrown = catchException(() -> {
			controllerProxy.endpointWithHandlerThrowing(originalException);
		});


		assertThat(thrown).isEqualTo(originalException);
	}

	@Test
	void handleDefaultExceptions_shouldDoNothing_whenProxyIsNotController() {
		IllegalStateException originalException = new IllegalStateException();


		Exception thrown = catchException(() -> {
			componentProxy.methodWithHandlerThrowing(originalException);
		});


		assertThat(thrown).isEqualTo(originalException);
	}



	@Test
	void handleDefaultExceptionsAsGroups_shouldWrapThrownException_whenThrownExceptionIsSpecified() {
		IllegalStateException originalException1 = new IllegalStateException();
		IllegalArgumentException originalException2 = new IllegalArgumentException();


		DefaultHandledException thrown1 = catchThrowableOfType(() -> {
			controllerProxy.endpointWithMultipleHandlersThrowing(originalException1);
		}, DefaultHandledException.class);

		DefaultHandledException thrown2 = catchThrowableOfType(() -> {
			controllerProxy.endpointWithMultipleHandlersThrowing(originalException2);
		}, DefaultHandledException.class);


		assertThat(thrown1.getResponseStatus()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(thrown1).hasCause(originalException1);

		assertThat(thrown2.getResponseStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(thrown2).hasCause(originalException2);
	}

	@Test
	void handleDefaultExceptionsAsGroups_shouldWrapThrownException_whenNoExceptionIsSpecified() {
		IllegalStateException originalExceptionWithSpecifiedStatus = new IllegalStateException();
		NullPointerException originalExceptionWithNoSpecifiedStatus = new NullPointerException();


		DefaultHandledException thrown1 = catchThrowableOfType(() -> {
			controllerProxy.endpointWithMultipleHandlersIncludingGeneralThrowing(originalExceptionWithSpecifiedStatus);
		}, DefaultHandledException.class);

		DefaultHandledException thrown2 = catchThrowableOfType(() -> {
			controllerProxy.endpointWithMultipleHandlersIncludingGeneralThrowing(originalExceptionWithNoSpecifiedStatus);
		}, DefaultHandledException.class);


		assertThat(thrown1.getResponseStatus()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(thrown1).hasCause(originalExceptionWithSpecifiedStatus);

		assertThat(thrown2.getResponseStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(thrown2).hasCause(originalExceptionWithNoSpecifiedStatus);
	}

	@Test
	void handleDefaultExceptionsAsGroups_shouldDoNothing_whenThrownExceptionIsNotSpecified() {
		NullPointerException originalException = new NullPointerException();


		Exception thrown = catchException(() -> {
			controllerProxy.endpointWithMultipleHandlersThrowing(originalException);
		});


		assertThat(thrown).isEqualTo(originalException);
	}

	@Test
	void handleDefaultExceptionsAsGroups_shouldDoNothing_whenProxyIsNotController() {
		IllegalStateException originalException = new IllegalStateException();


		Exception thrown = catchException(() -> {
			componentProxy.methodWithMultipleHandlersThrowing(originalException);
		});


		assertThat(thrown).isEqualTo(originalException);
	}



	@Controller
	static class TestController {

		@DefaultExceptionHandler(
				exceptions = IllegalStateException.class,
				responseStatus = HttpStatus.NOT_FOUND
		)
		public void endpointWithHandlerThrowing(RuntimeException exception) {
			throw exception;
		}

		@DefaultExceptionHandler(
				responseStatus = HttpStatus.NOT_FOUND
		)
		public void endpointWithGeneralHandlerThrowing(RuntimeException exception) {
			throw exception;
		}


		@DefaultExceptionHandler(
				exceptions = IllegalStateException.class,
				responseStatus = HttpStatus.NOT_FOUND
		)
		@DefaultExceptionHandler(
				exceptions = {IllegalArgumentException.class, IllegalStateException.class},
				responseStatus = HttpStatus.BAD_REQUEST
		)
		public void endpointWithMultipleHandlersThrowing(RuntimeException exception) {
			throw exception;
		}

		@DefaultExceptionHandler
		@DefaultExceptionHandler(
				exceptions = IllegalStateException.class,
				responseStatus = HttpStatus.NOT_FOUND
		)
		public void endpointWithMultipleHandlersIncludingGeneralThrowing(RuntimeException exception) {
			throw exception;
		}

	}


	static class TestComponent {

		@DefaultExceptionHandler
		public void methodWithHandlerThrowing(RuntimeException exception) {
			throw exception;
		}

		@DefaultExceptionHandler(exceptions = IllegalStateException.class)
		@DefaultExceptionHandler
		public void methodWithMultipleHandlersThrowing(RuntimeException exception) {
			throw exception;
		}

	}

}