package dev.roanoke.rib.utils

import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

object FileUtils {
    /**
     * Copies a resource from the internal JAR to an external file path.
     * The copying process can optionally overwrite existing files based on the provided parameter.
     *
     * @param resourcePath The path to the internal resource (e.g., "/ggyms/gui/gyms.json").
     * @param outputPath The external Path object where the resource should be saved.
     * @param overwrite If true, the file at outputPath will be overwritten if it exists.
     * @throws IOException If an I/O error occurs.
     */
    fun copyResourceToFile(resourcePath: String, outputPath: Path, overwrite: Boolean = false) {
        Files.createDirectories(outputPath.parent)

        if (!Files.exists(outputPath) || overwrite) {
            FileUtils::class.java.getResourceAsStream(resourcePath)?.use { inputStream ->
                Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING)
            } ?: throw IllegalArgumentException("Resource not found: $resourcePath")
        }
    }
}
