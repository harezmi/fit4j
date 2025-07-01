package com.udemy.libraries.acceptancetests

import com.udemy.libraries.acceptancetests.context.AcceptanceTestExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@IntegrationTest
@ActiveProfiles(value = ["test","acceptancetest"])
@TestPropertySource(properties = ["udemy.test.isAcceptanceTestClass=true"])
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(AcceptanceTestExtension::class)
annotation class AcceptanceTest(val fixtureFilePath: String = "")
