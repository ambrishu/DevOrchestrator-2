package core.configuration

import models.AdoConfiguration
import java.nio.file.Path

/** Loads and parses ADO configuration from a YAML file. */
interface ConfigurationLoader {

    /** Returns true if a configuration file exists at [path]. */
    fun exists(path: Path): Boolean

    /**
     * Loads and parses the configuration file at [path].
     *
     * @throws core.common.exception.ConfigurationException if the file is missing, unreadable, or malformed.
     */
    fun load(path: Path): AdoConfiguration
}
