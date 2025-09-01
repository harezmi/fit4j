package com.udemy.libraries.acceptancetests.legacy_api.experimentation

interface FeatureVariant {
    fun getVariableString(name:String, defaultValue:String) : String
}