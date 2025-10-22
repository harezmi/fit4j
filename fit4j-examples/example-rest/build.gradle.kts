val retrofitVersion: String by project
dependencies{
   testImplementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
   testImplementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
}

