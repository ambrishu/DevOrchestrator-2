package cli

import com.github.ajalt.clikt.core.CliktCommand
import utils.AdoMetadata

/** `ado version` — prints the ADO version. */
class VersionCommand : CliktCommand(name = "version", help = "Print the ADO version") {

    override fun run() {
        echo("ado version ${AdoMetadata.VERSION}")
    }
}
