plugins {
    id("java")
    id ("io.spring.dependency-management") version "1.1.3"
    id ("org.springframework.boot") version "3.1.4"
}
group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot", "spring-boot-starter-web")
    implementation("org.springframework.boot" , "spring-boot-starter-data-jpa")
}

dependencies {
    implementation("org.postgresql", "postgresql", "42.2.19")
}

dependencies {
    compileOnly ("org.projectlombok:lombok:1.18.20")
    testCompileOnly ("org.projectlombok:lombok:1.18.20")
    annotationProcessor ("org.projectlombok:lombok:1.18.20")
    testAnnotationProcessor ("org.projectlombok:lombok:1.18.20")
}

/**
 * Tests
 */
dependencies {
    testImplementation ("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}