package dyamo.narek.syntechnica.global;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ConfigurationPropertyHolders {

	private ConfigurationPropertyHolders() {}


	@NotNull
	public static <T> T configProperties(@NotNull Class<T> configurationPropertiesClass, @NotNull String... configs) {
		String propertiesPrefix = extractPropertiesPrefix(configurationPropertiesClass);

		Binder binder = getConfiguredBinder(configurationPropertiesClass, getPropertiesFrom(configs));

		return binder.bind(propertiesPrefix, configurationPropertiesClass).get();
	}

	@NotNull
	public static <T> T configProperties(@NotNull Class<T> configurationPropertiesClass) {
		return configProperties(configurationPropertiesClass,
				"application.yml", "application-common.yml", "application-dev.yml", "application-test.yml"
		);
	}


	private static String extractPropertiesPrefix(Class<?> configurationPropertiesClass) {
		var propertiesAnnotation = configurationPropertiesClass.getAnnotation(ConfigurationProperties.class);
		if (propertiesAnnotation == null) {
			throw new IllegalArgumentException("Specified class is not annotated as ConfigurationProperties");
		}

		String prefix = propertiesAnnotation.prefix();
		if (prefix.isBlank())
			prefix = propertiesAnnotation.value();

		return prefix;
	}

	private static Properties getPropertiesFrom(String... configs) {
		YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();

		var resources = Arrays.stream(configs).map(ClassPathResource::new).toArray(ClassPathResource[]::new);
		factoryBean.setResources(resources);

		return factoryBean.getObject();
	}

	private static <T> Binder getConfiguredBinder(Class<T> configurationPropertiesClass, Properties properties) {
		var configurationPropertySource = new MapConfigurationPropertySource(properties);
		PropertySource<?> propertySource = new PropertiesPropertySource(
				StringUtils.capitalize(configurationPropertiesClass.getSimpleName()), properties
		);

		var placeholdersResolver = new PropertySourcesPlaceholdersResolver(List.of(propertySource));

		return new Binder(List.of(configurationPropertySource), placeholdersResolver);
	}

}
