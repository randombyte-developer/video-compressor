package de.randombyte.videocompressor

import java.io.File
import java.nio.file.Files
import kotlin.system.exitProcess

const val ORIGINAL = "original"
const val COMPRESSED = "compressed"

fun main(args: Array<String>) {
    if (args.size != 2) {
        println("Args: <base-path> <cq-value>")
        exitProcess(0)
    }

    val basePath = File(args[0])
    val cqValue = args[1]

    val originalBase = basePath.resolve(ORIGINAL)
    val compressedBase = basePath.resolve(COMPRESSED)

    // .ts -> .mp4
    originalBase.listFiles().orEmpty()
        .filter { it.extension == "ts" }
        .forEach { it.renameTo(File(it.absolutePath.replaceAfterLast(".", "mp4"))) }

    originalBase.walk()
        .filter { it.extension == "mp4" }
        .forEach { originalFile ->
            val relativePath = originalFile.relativeTo(originalBase)
            val compressedFile = compressedBase.resolve(relativePath)

            if (compressedFile.exists()) return@forEach // continue

            Files.createDirectories(compressedFile.toPath().parent)

            println("Compressing $originalFile to $compressedFile")
            val exitCode = ProcessBuilder("ffmpeg",
                "-i", originalFile.absolutePath,
                "-c:v", "h264_nvenc", "-cq", cqValue,
                "-c:a", "libopus",
                "-strict", "-2", // to allow opus in MP4
                compressedFile.absolutePath
            )
                .redirectOutput(ProcessBuilder.Redirect.INHERIT) // show in console
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
                .waitFor()

            if (exitCode != 0) compressedFile.delete() // remove file if the compressing process failed
        }
}