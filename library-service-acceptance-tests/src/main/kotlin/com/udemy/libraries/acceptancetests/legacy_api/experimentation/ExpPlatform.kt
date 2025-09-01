package com.udemy.libraries.acceptancetests.legacy_api.experimentation

interface ExpPlatform {
    fun getFeatureVariant(name:String) : FeatureVariant
}