plugins {
    id("java")
    id("org.springframework.boot")version("2.6.2")
    id("io.spring.dependency-management") version ("1.0.11.RELEASE")
    id("io.freefair.lombok") version ("5.3.0") // Lombokプラグインを追加
}
group = "org.example"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.projectlombok:lombok:1.18.26")
    implementation("org.json:json:20230618")
    implementation("org.json:org.json:chargebee-1.0")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-tomcat")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.epages:wiremock-spring-boot-starter:0.8.5")
    implementation("com.h2database:h2")
    implementation("org.json:json:20210307")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.4.2.Final")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.github.tomakehurst:wiremock:2.30.1")

}

tasks.test {
    useJUnitPlatform()
}

