package core.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import core.common.exception.ConfigurationException
import io.github.oshai.kotlinlogging.KotlinLogging
import models.AdoConfiguration
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

private val logger = KotlinLogging.logger {}

/** [ConfigurationLoader] backed by Jackson YAML. */
class YamlConfigurationLoader(
    private val mapper: ObjectMapper = ObjectMapper(YAMLFactory()).registerKotlinModule(),
    private val validator: ConfigurationValidator = DefaultConfigurationValidator(),
) : ConfigurationLoader {

    override fun exists(path: Path): Boolean = path.exists() && path.isRegularFile()

    override fun load(path: Path): AdoConfiguration {
        if (!exists(path)) {
            throw ConfigurationException("Configuration file not found: $path")
        }

        logger.debug { "Loading configuration from $path" }

        val config = try {
            val content = Files.readString(path)
            if (content.isBlank()) {
                AdoConfiguration()
            } else {
                mapper.readValue(content, AdoConfiguration::class.java)
            }
        } catch (e: Exception) {
            throw ConfigurationException("Failed to parse configuration file: $path", e)
        }

        val result = validator.validate(config)
        if (!result.isValid) {
            throw ConfigurationException(
                "Configuration file is invalid: $path\n" + result.errors.joinToString("\n") { "  - $it" },
            )
        }

        return config
    }
}
