package org.fit4j.testcontainers.support

import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

class InitCommandRecordingContainer(image: DockerImageName) :
    GenericContainer<InitCommandRecordingContainer>(image) {

    val initCommands = mutableListOf<String>()

    fun withInitCommand(vararg commands: String): InitCommandRecordingContainer {
        initCommands.addAll(commands)
        return self()
    }
}
