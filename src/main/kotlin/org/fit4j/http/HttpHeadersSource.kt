package org.fit4j.http

import org.springframework.http.HttpHeaders
import java.util.function.Supplier

@FunctionalInterface
fun interface HttpHeadersSource : Supplier<HttpHeaders> {
}