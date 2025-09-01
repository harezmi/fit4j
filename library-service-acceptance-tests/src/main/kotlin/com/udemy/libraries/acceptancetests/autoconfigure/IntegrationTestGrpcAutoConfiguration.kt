package com.udemy.libraries.acceptancetests.autoconfigure

import com.example.UserRetrievalServiceOuterClass
import com.udemy.libraries.acceptancetests.EnableOnIntegrationTestClass
import com.udemy.libraries.acceptancetests.legacy_api.requestcontext.RequestContextInterceptor
import com.udemy.libraries.acceptancetests.legacy_api.requestcontext.RequestContextMarshaller
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorConfigurer
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer
import net.devh.boot.grpc.server.serverfactory.InProcessGrpcServerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@AutoConfiguration
@AutoConfigureAfter(GrpcServerFactoryAutoConfiguration::class)
@ConditionalOnBean(InProcessGrpcServerFactory::class)
@EnableOnIntegrationTestClass
class IntegrationTestGrpcAutoConfiguration {
    /*
    RequestContextInterceptor is also being added by the RequestContextAutoconfigure of requestcontext library,
    but somehow it is not being added to the test grpc server, so adding it here as well. At worst case it will
    be added twice without any side effects.
     */
    @Bean
    fun requestContextGrpcServerConfigurer() : GrpcServerConfigurer {
        return GrpcServerConfigurer { t -> t.intercept(RequestContextInterceptor()) }
    }

    @Bean
    fun serviceRequestContextGlobalClientInterceptorConfigurer(applicationContext: ApplicationContext) : GlobalClientInterceptorConfigurer {
        return GlobalClientInterceptorConfigurer { interceptors ->
            val beansMap = applicationContext.getBeansOfType(UserRetrievalServiceOuterClass.ServiceRequestContext::class.java)
            if (beansMap.isNotEmpty()) {
                val serviceRequestContext = beansMap.firstNotNullOf { it.value }
                val metadata = Metadata()
                metadata.put(
                    Metadata.Key.of("requestContext-bin", RequestContextMarshaller()),
                    serviceRequestContext
                )
                val interceptor = MetadataUtils.newAttachHeadersInterceptor(metadata)
                interceptors.add(interceptor)
            }
        }
    }
}