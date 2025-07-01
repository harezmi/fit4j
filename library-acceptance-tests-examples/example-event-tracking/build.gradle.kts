val udemyEventTrackerVersion : String by project
val udemyChatResponseGeneratedVersion : String by project
dependencies{
    // dependencies for the project in addition to parent project dependencies
    testImplementation("com.udemy.libraries.eventtracking:eventtracker:$udemyEventTrackerVersion")
    testImplementation("com.udemy.eventtracking.events:chatresponsegenerated:$udemyChatResponseGeneratedVersion")
}
