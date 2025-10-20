package com.fit4j

import com.fit4j.context.AcceptanceTestExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@IT
@ActiveProfiles(value = ["test","acceptancetest"])
@TestPropertySource(properties = ["fit4j.isAcceptanceTestClass=true"])
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(AcceptanceTestExtension::class)
annotation class FIT(val fixtureFilePath: String = "")
