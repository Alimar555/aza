# Spring Boot 3.0 Security with JWT Implementation

A robust Spring Boot application implementing JWT-based authentication and security features with modern practices.

## 🚀 Features
* User registration and login with JWT authentication
* Two-factor authentication (2FA) support
* Password encryption using BCrypt
* Role-based authorization with Spring Security
* Email verification system
* Customized access denied handling
* Logout mechanism
* Refresh token functionality
* OpenAPI documentation (Swagger)

## 🛠 Technologies
* Java 17
* Spring Boot 3.4.0
* Spring Security
* JSON Web Tokens (JWT)
* PostgreSQL
* Maven
* Lombok
* Thymeleaf
* SpringDoc OpenAPI (Swagger)

## 📋 Prerequisites
* JDK 17+
* Maven 3+
* PostgreSQL

## 🔧 Dependencies
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.0</version>
</parent>

<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    
    <!-- Other dependencies as in your pom.xml -->
</dependencies>
