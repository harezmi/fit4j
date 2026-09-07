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

            dsl.path("/java/predicate")
                .predicate("#request.body == 'java-body'")
                .respond(response -> response
                    .status(203)
                    .bodyAsText("java-predicate-match"));

            dsl.path("/java/body/exact")
                .method("POST")
                .body("java-exact")
                .respond(response -> response
                    .status(204)
                    .bodyAsText("java-body-exact"));

            dsl.path("/java/body/json")
                .method("POST")
                .bodyAsJson(Map.of("message", "hello", "count", 2))
                .respond(response -> response
                    .status(205)
                    .bodyAsText("java-body-json"));

            dsl.path("/java/query/{id}")
                .pathVariable("id", "123")
                .queryParam("filter", "active")
                .bodyAbsent()
                .respond(response -> response
                    .status(206)
                    .bodyAsText("java-query-match"));

            dsl.path("/java/rich-match/{id}")
                .pathVariable("id", "123")
                .headerContains("X-Trace", "trace-123")
                .headerMatches("X-Request-Id", "req-\\d+")
                .queryParamContains("filter", "act")
                .queryParamRegex("version", "\\d+")
                .respond(response -> response
                    .status(207)
                    .bodyAsText("java-rich-match"));
        });

        HttpResponse hello = (HttpResponse) mockResponseFactory.getResponseFor(new HttpRequest("/java/hello", "GET", "", Map.of(), "/java/hello"));
        HttpResponse json = (HttpResponse) mockResponseFactory.getResponseFor(new HttpRequest("/java/json", "GET", "", Map.of(), "/java/json"));
        HttpResponse first = (HttpResponse) mockResponseFactory.getResponseFor(new HttpRequest("/java/sequence", "GET", "", Map.of(), "/java/sequence"));
        HttpResponse second = (HttpResponse) mockResponseFactory.getResponseFor(new HttpRequest("/java/sequence", "GET", "", Map.of(), "/java/sequence"));
        HttpResponse predicate = (HttpResponse) mockResponseFactory.getResponseFor(new HttpRequest("/java/predicate", "POST", "java-body", Map.of(), "/java/predicate"));
        HttpResponse exactBody = (HttpResponse) mockResponseFactory.getResponseFor(new HttpRequest("/java/body/exact", "POST", "java-exact", Map.of(), "/java/body/exact"));
        HttpResponse jsonBody = (HttpResponse) mockResponseFactory.getResponseFor(new HttpRequest("/java/body/json", "POST", "{\"count\":2,\"message\":\"hello\"}", Map.of(), "/java/body/json"));
        HttpResponse query = (HttpResponse) mockResponseFactory.getResponseFor(new HttpRequest("/java/query/123", "GET", "", Map.of(), "/java/query/123?filter=active&debug=true"));
        HttpResponse richMatch = (HttpResponse) mockResponseFactory.getResponseFor(new HttpRequest("/java/rich-match/123", "GET", "", Map.of("X-Trace", "pre-trace-123-post", "X-Request-Id", "req-456"), "/java/rich-match/123?filter=active&version=42"));

        Assertions.assertEquals(207, hello.getStatusCode());
        Assertions.assertEquals("hello-from-java", hello.bodyAsText());
        Assertions.assertEquals("text/plain", hello.getHeaders().get("Content-Type"));

        JSONAssert.assertEquals("{\"message\":\"hello\",\"count\":2}", json.bodyAsText(), JSONCompareMode.LENIENT);
        Assertions.assertEquals(201, first.getStatusCode());
        Assertions.assertEquals("first", first.bodyAsText());
        Assertions.assertEquals(202, second.getStatusCode());
        Assertions.assertEquals("second", second.bodyAsText());
        Assertions.assertEquals(203, predicate.getStatusCode());
        Assertions.assertEquals("java-predicate-match", predicate.bodyAsText());
        Assertions.assertEquals(204, exactBody.getStatusCode());
        Assertions.assertEquals("java-body-exact", exactBody.bodyAsText());
        Assertions.assertEquals(205, jsonBody.getStatusCode());
        Assertions.assertEquals("java-body-json", jsonBody.bodyAsText());
        Assertions.assertEquals(206, query.getStatusCode());
        Assertions.assertEquals("java-query-match", query.bodyAsText());
        Assertions.assertEquals(207, richMatch.getStatusCode());
        Assertions.assertEquals("java-rich-match", richMatch.bodyAsText());
    }
}
