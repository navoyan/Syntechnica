plugins {
	java
	id("org.springframework.boot") version "3.1.0"
	id("io.spring.dependency-management") version "1.1.0"
}


val lowercaseProjectName = project.name.lowercase()
group = "syntechnica"
version = "0.0.1"


java {
	sourceCompatibility = JavaVersion.VERSION_20
}

tasks.bootBuildImage {
	imageName.set(lowercaseProjectName)
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}


repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-hateoas")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.bouncycastle:bcpkix-jdk18on:1.75")

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	implementation("org.flywaydb:flyway-core")

	implementation("org.springframework.boot:spring-boot-starter-validation")

	implementation("org.springframework.boot:spring-boot-starter-integration")
	implementation("org.springframework.integration:spring-integration-file")


	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")


	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")


	runtimeOnly("org.postgresql:postgresql")


	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")


	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.integration:spring-integration-test")
	testImplementation("org.springframework.security:spring-security-test")

	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
}


tasks.withType<Test> {
	useJUnitPlatform()
}
