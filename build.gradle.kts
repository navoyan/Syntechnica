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
	sourceCompatibility = JavaVersion.VERSION_17
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

	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.bouncycastle:bcpkix-jdk18on:1.75")

	runtimeOnly("org.postgresql:postgresql")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.flywaydb:flyway-core")

	implementation("org.springframework.boot:spring-boot-starter-validation")

	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("com.github.ben-manes.caffeine:caffeine")


	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")


	developmentOnly("org.springframework.boot:spring-boot-docker-compose")


	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")


	asciiDoctorExt("org.springframework.restdocs:spring-restdocs-asciidoctor")


	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")

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
