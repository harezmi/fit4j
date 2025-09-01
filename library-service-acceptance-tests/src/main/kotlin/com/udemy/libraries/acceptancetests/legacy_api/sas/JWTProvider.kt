package com.udemy.libraries.acceptancetests.legacy_api.sas

interface JWTProvider {
    fun getJwt(): String
}