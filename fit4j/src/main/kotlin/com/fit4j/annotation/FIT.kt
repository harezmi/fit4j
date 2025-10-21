package com.fit4j.annotation

import com.fit4j.context.AcceptanceTestExtension
import org.junit.jupiter.api.extension.ExtendWith
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
annotation class IT(val webEnvironment : SpringBootTest.WebEnvironment = SpringBootTest.WebEnvironment.MOCK)

@IT
@TestPropertySource(properties = ["fit4j.isFunctionalIntegrationTestClass=true"])
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(AcceptanceTestExtension::class)
annotation class FIT(val fixtureFilePath: String = "", val webEnvironment: SpringBootTest.WebEnvironment = SpringBootTest.WebEnvironment.MOCK)

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
annotation class FixtureForFIT(val name: String)
