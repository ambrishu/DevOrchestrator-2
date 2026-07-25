package utils

import core.common.exception.ProcessExecutionException
import models.ProcessResult
import java.io.IOException
import java.nio.file.Path

/** [ProcessExecutor] backed by [ProcessBuilder]. */
class DefaultProcessExecutor : ProcessExecutor {

    override fun execute(command: List<String>, workingDirectory: Path): ProcessResult {
        val start = System.currentTimeMillis()

        val process = try {
            ProcessBuilder(command).directory(workingDirectory.toFile()).start()
        } catch (e: IOException) {
            throw ProcessExecutionException("Failed to start process: ${command.joinToString(" ")}", e)
        }

        // ADO never sends stdin. Closing it immediately signals EOF instead of leaving children
        // that check for piped input (e.g. `claude --print`) waiting out a timeout guessing.
        process.outputStream.close()

        val stderr = StringBuilder()
        val stderrThread = Thread {
            process.errorStream.bufferedReader().forEachLine { stderr.appendLine(it) }
        }
        stderrThread.start()

        val stdout = StringBuilder()
        process.inputStream.bufferedReader().forEachLine { stdout.appendLine(it) }
        stderrThread.join()

        val exitCode = process.waitFor()

        return ProcessResult(
            exitCode = exitCode,
            stdout = stdout.toString().trimEnd(),
            stderr = stderr.toString().trimEnd(),
            durationMillis = System.currentTimeMillis() - start,
        )
    }
}
