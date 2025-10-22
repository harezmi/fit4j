package com.fit4j.grpc

import com.google.protobuf.Descriptors.Descriptor
import org.springframework.context.ApplicationContext
import org.springframework.util.ReflectionUtils

class GrpcTypeDescriptorsProvider(private val applicationContext: ApplicationContext, private val grpcClassScanner: GrpcClassScanner) {
    private var descriptors:List<Descriptor> = discoverDescriptors()

    private fun discoverDescriptors() : List<Descriptor> {
        val descriptors = mutableSetOf<Descriptor>()
        val explicitlyDefinedDescriptors = getExplicitlyDefinedDescriptors()

        descriptors.addAll(explicitlyDefinedDescriptors)

        val scanDescriptorsEnabled = applicationContext.environment
            .getProperty("fit4j.grpc.scanDescriptorsEnabled","true").toBoolean()

        if(scanDescriptorsEnabled) {
            val implicitlyScannedDescriptors = getImplicitlyScannedDescriptors()
            descriptors.addAll(implicitlyScannedDescriptors)
        }

        return descriptors.toList()
    }

    private fun getImplicitlyScannedDescriptors(): MutableList<Descriptor> {
        val implicitlyScannedDescriptors = mutableListOf<Descriptor>()
        val classNames = grpcClassScanner.scanTypeDescriptorsFromClasspath()
        classNames.forEach {
            val classz = Class.forName(it.className)
            val method = ReflectionUtils.findMethod(classz, "getDescriptor")
            if (method != null) {
                val descriptor = ReflectionUtils.invokeMethod(method, classz) as Descriptor
                implicitlyScannedDescriptors.add(descriptor)
            }
        }
        return implicitlyScannedDescriptors
    }

    private fun getExplicitlyDefinedDescriptors() = applicationContext.getBeansOfType(List::class.java)
        .values.filter { it.isNotEmpty() }.flatten().filterIsInstance<Descriptor>()

    fun getDescriptors() : List<Descriptor> {
        return descriptors
    }
}