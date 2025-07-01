val udemySASVersion: String by project
val retrofitVersion: String by project
dependencies{
   testImplementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
   testImplementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
   testImplementation("com.udemy.libraries.sas:sas-core:$udemySASVersion")
   testImplementation("com.udemy.libraries.sas:sas-autoconfigure:$udemySASVersion")
}

