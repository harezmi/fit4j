package com.fit4j.mock.declarative

import com.fit4j.annotation.FIT
import com.fit4j.annotation.FixtureForFIT
import com.fit4j.context.AcceptanceTestContextManager
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.test.context.event.annotation.AfterTestMethod
import org.yaml.snakeyaml.Yaml

class DeclarativeTestFixtureProvider(private val applicationContext: ApplicationContext,
                                     declarativeTestFixtureBuilders: List<DeclarativeTestFixtureBuilder>,
                                     resourcePath: String = "classpath:acceptance-tests-fixtures.yml"
) {

    private var defaultTestFixturesMap: MutableMap<String, TestFixturesGroup> = mutableMapOf()
    private val testFixtureBuilderMap:MutableMap<String,DeclarativeTestFixtureBuilder> = mutableMapOf()

    private val testFixturesMapCache: MutableMap<String,MutableMap<String, TestFixturesGroup>> = mutableMapOf()

    init {
        declarativeTestFixtureBuilders.forEach {
            testFixtureBuilderMap.put(it.protocol(),it)
        }
        defaultTestFixturesMap = loadTestFixtureMap(resourcePath)
    }

    private fun loadTestFixtureMap(resourcePath: String): MutableMap<String, TestFixturesGroup> {
        if(testFixturesMapCache.containsKey(resourcePath)) return testFixturesMapCache.get(resourcePath)!!

        val testFixturesMap = mutableMapOf<String, TestFixturesGroup>()

        val resource = applicationContext.getResource(resourcePath)
        if (resource.exists()) {
            val yaml = Yaml()
            val testFixtureDefinitions: Map<String, List<Map<String, Any>>> = yaml.load(resource.inputStream)
            testFixtureDefinitions["tests"]!!.forEach {
                val testFixtures: TestFixturesGroup = buildTestFixtures(it)
                testFixturesMap[testFixtures.name] = testFixtures
            }
            if (testFixturesMap.containsKey("*")) {
                testFixturesMap.forEach {
                    if (it.key != "*") {
                        it.value.globalTestFixtures = testFixturesMap["*"]
                    }
                }
            }
        }
        testFixturesMapCache.put(resourcePath,testFixturesMap)
        return testFixturesMap
    }

    private fun buildTestFixtures(it:Map<String,Any>) : TestFixturesGroup {
        val name = it["name"] as String?
        if(name.isNullOrBlank()) {
            throw IllegalStateException("name is required")
        }
        val testFixtureElements = mutableListOf<TestFixture>()
        val fixtureElements = it["fixtures"] as List<Map<String,Any>>
        fixtureElements.forEach {
            val testFixtureElement = buildTestFixture(it)
            testFixtureElements.add(testFixtureElement)
        }
        return TestFixturesGroup(name, testFixtureElements)
    }

    private fun buildTestFixture(fixtureElement:Map<String,Any>) : TestFixture {
        val requestMap = fixtureElement["request"] as Map<String,Any>
        if(requestMap.containsKey("protocol").not()) {
            throw IllegalStateException("protocol is required")
        }
        val protocol = (requestMap["protocol"] as String).uppercase()

        if(testFixtureBuilderMap.containsKey(protocol).not()) {
            throw IllegalStateException("Protocol not supported $protocol, available protocols are ${testFixtureBuilderMap.keys}")
        }

        val testFixtureBuilder = testFixtureBuilderMap.get(protocol)

        return testFixtureBuilder!!.build(requestMap)
    }

    fun getTestFixtures(testName:String) : TestFixturesGroup? {
        return if(defaultTestFixturesMap.containsKey(testName)) {
            defaultTestFixturesMap[testName]
        } else if(defaultTestFixturesMap.containsKey("*")) {
            defaultTestFixturesMap["*"]
        } else {
            null
        }
    }

    private fun getTestFixtures(
        fixtureName: String?,
        testFixturesMap: Map<String, TestFixturesGroup>
    ): TestFixturesGroup? {
        return if (testFixturesMap.containsKey(fixtureName)) {
            testFixturesMap[fixtureName]
        } else if (testFixturesMap.containsKey("*")) {
            testFixturesMap["*"]
        } else {
            null
        }
    }

    fun getTestFixturesForCurrentTest() : TestFixturesGroup? {
        val clazz = AcceptanceTestContextManager.getTestClass()
        if(clazz != null) {
            val fixtureFilePath = AnnotationUtils.findAnnotation(clazz, FIT::class.java)?.fixtureFilePath
            if (!fixtureFilePath.isNullOrEmpty()) {
                val testFixtureMap = loadTestFixtureMap(fixtureFilePath)
                val testMethod = AcceptanceTestContextManager.getTestMethod() ?:
                    throw IllegalStateException("Test method not found within current test class ${clazz.simpleName}")
                val testFixtureAnnotation = AnnotationUtils.findAnnotation(testMethod, FixtureForFIT::class.java)
                if(testFixtureAnnotation != null) {
                    val fixtureName = testFixtureAnnotation.name
                    if (fixtureName.isBlank()) {
                        throw IllegalStateException("Fixture name is required for test method ${testMethod.name}")
                    }
                    val testFixtures = this.getTestFixtures(fixtureName, testFixtureMap)
                        ?: throw IllegalStateException("Fixture $fixtureName not found in $fixtureFilePath")
                    return testFixtures
                }
            }
        }
        val testName = applicationContext.environment.getProperty("fit4j.acceptanceTestClass.simpleName") ?: return null
        return this.getTestFixtures(testName)
    }

    @AfterTestMethod
    fun reset() {
        defaultTestFixturesMap.forEach { it.value.reset() }
        testFixturesMapCache.values.forEach { it.values.forEach { testFixturesGroup -> testFixturesGroup.reset() } }
    }
}
