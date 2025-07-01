package com.udemy.libraries.acceptancetests.helpers

import org.springframework.context.ApplicationContext
import org.springframework.core.env.PropertyResolver
import org.springframework.core.io.Resource
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.core.type.ClassMetadata
import org.springframework.core.type.classreading.CachingMetadataReaderFactory
import org.springframework.core.type.classreading.MetadataReaderFactory
import org.springframework.core.type.filter.TypeFilter
import org.springframework.util.ClassUtils

class ClassScanner (applicationContext: ApplicationContext) {

    private var propertyResolver: PropertyResolver = applicationContext.environment
    private var resourcePatternResolver: ResourcePatternResolver = applicationContext
    private var metadataReaderFactory: MetadataReaderFactory = CachingMetadataReaderFactory(resourcePatternResolver)

    fun getClassNames(typeFilter: TypeFilter, vararg basePackagePatterns: String): List<ClassMetadata> {
        val classNames = mutableListOf<ClassMetadata>()
        for (basePackagePattern in basePackagePatterns) {
            val resources = resolveResources(basePackagePattern)

            for (resource in resources) {
                classNames.addAll(processResource(resource, typeFilter))
            }
        }
        return classNames
    }

    private fun resolveResources(basePackagePattern: String): Array<out Resource> {
        val resourcePattern = "classpath*:" + resolveBasePackage(basePackagePattern) + "/*.class"
        val resources = resourcePatternResolver.getResources(resourcePattern)
        return resources
    }

    private fun processResource(resource: Resource, typeFilter: TypeFilter) : List<ClassMetadata> {
        val classNames = mutableListOf<ClassMetadata>()
        val metadataReader = metadataReaderFactory.getMetadataReader(resource)
        if (typeFilter.match(metadataReader, metadataReaderFactory)) {
            classNames.add(metadataReader.classMetadata)
        }
        return classNames
    }

    private fun resolveBasePackage(basePackage: String): String {
        return ClassUtils.convertClassNameToResourcePath(propertyResolver.resolveRequiredPlaceholders(basePackage))
    }
}