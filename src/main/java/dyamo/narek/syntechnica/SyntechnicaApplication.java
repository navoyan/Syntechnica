package dyamo.narek.syntechnica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SyntechnicaApplication {
	public static void main(String[] args) {
		SpringApplication.run(SyntechnicaApplication.class, args);
	}
}
