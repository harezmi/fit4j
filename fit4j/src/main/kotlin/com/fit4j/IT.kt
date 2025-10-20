package com.fit4j

import org.junit.jupiter.api.Tag
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles(value = ["test"])
@TestPropertySource(properties=["spring.main.allow-bean-definition-overriding=true","fit4j.isIntegrationTestClass=true"])
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
annotation class IT
