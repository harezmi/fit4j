package org.fit4j.http;

import org.fit4j.Fit4J;
import org.fit4j.annotation.FIT;
import org.fit4j.mock.MockResponseFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@FIT
class HttpDslJavaFIT {

    @Autowired
    private MockResponseFactory mockResponseFactory;

    @Test
    void it_should_work_from_java() {
        Fit4J.http(dsl -> {
            dsl.path("/java/hello")
                .method("GET")
                .respond(response -> response
                    .status(207)
                    .header("Content-Type", "text/plain")
                    .bodyAsText("hello-from-java"));

            dsl.path("/java/json")
                .respond(response -> response
                    .status(200)
                    .bodyAsJson(Map.of("message", "hello", "count", 2)));

            dsl.path("/java/sequence")
                .responds(sequence -> sequence
                    .response(response -> response.status(201).bodyAsText("first"))
                    .response(response -> response.status(202).bodyAsText("second")));
        });

        HttpResponse hello = (HttpResponse) mockResponseFactory.getResponseFor(new HttpRequest("/java/hello", "GET", "", Map.of(), "/java/hello"));
        HttpResponse json = (HttpResponse) mockResponseFactory.getResponseFor(new HttpRequest("/java/json", "GET", "", Map.of(), "/java/json"));
        HttpResponse first = (HttpResponse) mockResponseFactory.getResponseFor(new HttpRequest("/java/sequence", "GET", "", Map.of(), "/java/sequence"));
        HttpResponse second = (HttpResponse) mockResponseFactory.getResponseFor(new HttpRequest("/java/sequence", "GET", "", Map.of(), "/java/sequence"));

        Assertions.assertEquals(207, hello.getStatusCode());
        Assertions.assertEquals("hello-from-java", hello.bodyAsText());
        Assertions.assertEquals("text/plain", hello.getHeaders().get("Content-Type"));

        JSONAssert.assertEquals("{\"message\":\"hello\",\"count\":2}", json.bodyAsText(), JSONCompareMode.LENIENT);
        Assertions.assertEquals(201, first.getStatusCode());
        Assertions.assertEquals("first", first.bodyAsText());
        Assertions.assertEquals(202, second.getStatusCode());
        Assertions.assertEquals("second", second.bodyAsText());
    }
}
