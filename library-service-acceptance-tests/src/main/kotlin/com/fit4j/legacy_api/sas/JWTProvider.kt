package com.fit4j.legacy_api.sas

interface JWTProvider {
    fun getJwt(): String
}