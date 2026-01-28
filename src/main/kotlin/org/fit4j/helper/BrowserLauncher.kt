package org.fit4j.helper

class BrowserLauncher {
    fun launch(url:String) {
        val os = System.getProperty("os.name").lowercase()
        val command = when {
            os.contains("mac") -> arrayOf("open", url)
            os.contains("win") -> arrayOf("cmd", "/c", "start", url)
            os.contains("nix") || os.contains("nux") -> arrayOf("xdg-open", url)
            else -> null
        }

        command?.let {
            Runtime.getRuntime().exec(it)
        }

        println("Browser is launched with url:$url. You need to terminate the test manually...")
        val latch = java.util.concurrent.CountDownLatch(1)
        latch.await()
    }
}