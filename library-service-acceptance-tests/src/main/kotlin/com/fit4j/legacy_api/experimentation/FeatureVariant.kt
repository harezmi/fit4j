package com.fit4j.legacy_api.experimentation

interface FeatureVariant {
    fun getVariableString(name:String, defaultValue:String) : String
}