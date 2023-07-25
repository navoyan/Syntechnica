package dyamo.narek.syntechnica.global;

import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.DefaultAopProxyFactory;

public class AopProxies {

	private AopProxies() {}


	@SuppressWarnings("unchecked")
	public static <T> T aopProxy(T target, Class<?>... aspectClasses) {
		AspectJProxyFactory aspectJProxyFactory = new AspectJProxyFactory(target);
		for (Class<?> aspectClass : aspectClasses) {
			aspectJProxyFactory.addAspect(aspectClass);
		}

		DefaultAopProxyFactory proxyFactory = new DefaultAopProxyFactory();
		AopProxy aopProxy = proxyFactory.createAopProxy(aspectJProxyFactory);

		return (T) aopProxy.getProxy();
	}

}
