package com.fit4j.legacy_api.experimentation

interface ExpPlatform {
    fun getFeatureVariant(name:String) : FeatureVariant
}