package com.fit4j.scope

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode

@Scope(scopeName = "test", proxyMode = ScopedProxyMode.TARGET_CLASS)
annotation class TestScoped
