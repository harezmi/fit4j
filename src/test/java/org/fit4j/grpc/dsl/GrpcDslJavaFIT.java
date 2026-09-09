package org.fit4j.grpc.dsl;

import com.example.fit4j.grpc.TestGrpc;
import com.google.protobuf.Message;
import io.grpc.Status;
import org.fit4j.Fit4J;
import org.fit4j.annotation.FIT;
import org.fit4j.grpc.GrpcResponseJsonBuilder;
import org.fit4j.mock.MockResponseFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@FIT
public class GrpcDslJavaFIT {

    @Autowired
    private MockResponseFactory mockResponseFactory;

    @TestConfiguration
    static class TestConfig {
        @Bean
        GrpcResponseJsonBuilder<Message> grpcResponseJsonBuilder() {
            return request -> {
                if (request instanceof TestGrpc.GetFooByIdRequest) {
                    return """
                        {
                          "foo": {
                            "id": 777,
                            "name": "builder"
                          }
                        }
                        """.trim();
                }
                return null;
            };
        }
    }

    @Test
    void itShouldAllowTheJavaGrpcDslToOverrideResponseBuilders() {
        Fit4J.grpc(grpc -> grpc.request(TestGrpc.GetFooByIdRequest.class, request -> request.respond(response -> response.bodyAsJson("""
            {
              "foo": {
                "id": 888,
                "name": "dsl"
              }
            }
            """))));

        TestGrpc.GetFooByIdRequest request = TestGrpc.GetFooByIdRequest.newBuilder().setId(11).build();
        TestGrpc.GetFooByIdResponse response = (TestGrpc.GetFooByIdResponse) mockResponseFactory.getResponseFor(request);

        Assertions.assertEquals(888L, response.getFoo().getId());
        Assertions.assertEquals("dsl", response.getFoo().getName());
    }
}
