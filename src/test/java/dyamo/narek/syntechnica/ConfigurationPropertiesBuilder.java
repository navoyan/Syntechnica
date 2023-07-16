package dyamo.narek.syntechnica;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;
import java.util.Properties;

public class ConfigurationPropertiesBuilder {

	private ConfigurationPropertiesBuilder() {}


	public static <T> T configProperties(Class<T> configurationPropertiesClass, String... configs) {
		ConfigurationProperties propertiesAnnotation = configurationPropertiesClass.getAnnotation(ConfigurationProperties.class);
		if (propertiesAnnotation == null)
			throw new IllegalArgumentException("Specified class is not annotated as ConfigurationProperties");


		YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();

		var resources = Arrays.stream(configs).map(ClassPathResource::new).toArray(ClassPathResource[]::new);
		factoryBean.setResources(resources);

		Properties properties = factoryBean.getObject();

		ConfigurationPropertySource propertySource = new MapConfigurationPropertySource(properties);
		Binder binder = new Binder(propertySource);

		String prefix = propertiesAnnotation.prefix();
		if (prefix.isBlank())
			prefix = propertiesAnnotation.value();

		return binder.bind(prefix, configurationPropertiesClass).get();
	}

	public static <T> T configProperties(Class<T> configurationPropertiesClass) {
		return configProperties(configurationPropertiesClass,
				"application.yml", "application-common.yml", "application-dev.yml", "application-test.yml"
		);
	}

}
