// Native test resource loader
// For native tests, we'll read from the file system

actual fun loadTestResource(path: String): String {
    // Native tests run from the project root, so we can read files directly
    val filePath = "src/commonTest/resources$path"
    return try {
        // This is a simplified version - in a real implementation,
        // you'd use platform-specific file reading
        readFileContent(filePath)
    } catch (e: Exception) {
        throw IllegalStateException("Could not load test resource: $path from $filePath", e)
    }
}

// Placeholder for file reading - would use okio or platform-specific APIs
private fun readFileContent(path: String): String {
    throw NotImplementedError("Native file reading not implemented for tests yet")
}

actual fun isJvmPlatform(): Boolean = false
