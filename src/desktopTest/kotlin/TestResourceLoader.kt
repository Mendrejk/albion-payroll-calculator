// Desktop (JVM) test resource loader

actual fun loadTestResource(path: String): String {
    val resource = object {}.javaClass.getResource(path)
        ?: throw IllegalStateException("Resource not found: $path")
    return resource.readText()
}

actual fun isJvmPlatform(): Boolean = true
