plugins {
	java
	id("org.springframework.boot") version "3.1.0"
	id("io.spring.dependency-management") version "1.1.0"
	id("org.asciidoctor.jvm.convert") version "3.3.2"
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


val asciiDoctorExt: Configuration by configurations.creating
val snippetsDirectory = file("build/generated-snippets")

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

	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("com.github.ben-manes.caffeine:caffeine")

	implementation("org.springframework.boot:spring-boot-starter-integration")
	implementation("org.springframework.integration:spring-integration-file")


	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")


	asciiDoctorExt("org.springframework.restdocs:spring-restdocs-asciidoctor")


	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")


	runtimeOnly("org.postgresql:postgresql")


	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")


	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.integration:spring-integration-test")
	testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")

	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
}


tasks.test {
	useJUnitPlatform()
	outputs.dir(snippetsDirectory)
}

tasks.asciidoctor {
	configurations("asciiDoctorExt")
	dependsOn(tasks.test)
	inputs.dir(snippetsDirectory)
}
