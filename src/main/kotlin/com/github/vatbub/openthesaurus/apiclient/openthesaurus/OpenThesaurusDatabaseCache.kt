/*-
 * #%L
 * Open Thesaurus Java View
 * %%
 * Copyright (C) 2019 - 2025 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.vatbub.openthesaurus.apiclient.openthesaurus

import com.github.vatbub.openthesaurus.logging.logger
import com.github.vatbub.openthesaurus.util.appDataFolder
import java.io.File
import java.net.URI
import java.net.URLConnection
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale
import kotlin.io.path.createDirectories

object OpenThesaurusDatabaseCache {
    val downloadUrl = URI("https://www.openthesaurus.de/export/OpenThesaurus-Textversion.zip")
    val zipDownloadDestination = appDataFolder.resolve("OpenThesaurus-Textversion.zip")
    val lastUpdatedFile = appDataFolder.resolve("lastUpdated.txt")

    private val lastModifiedFormatter by lazy {
        DateTimeFormatterBuilder()
            .appendPattern("EEE, dd MMM yyyy HH:mm:ss z")
            .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
            .toFormatter(Locale.ENGLISH)
    }

    fun downloadIfNecessary() {
        val connection = downloadUrl.toURL().openConnection()
        val lastModifiedOnServer = connection.getLastModifiedDate()
        val lastModifiedOnDisk = getLastModifiedFromFile()
        if (!zipDownloadDestination.exists() || lastModifiedOnDisk == null) {
            logger.info(
                "Downloading offline database to '${zipDownloadDestination.absolutePath}' " +
                        "because it has never been downloaded before..."
            )
            connection.downloadZipFile(zipDownloadDestination)
            setLastModifiedOnDisk(lastModifiedOnServer)
        } else if (lastModifiedOnServer.isAfter(lastModifiedOnDisk)) {
            logger.info(
                "Downloading offline database to '${zipDownloadDestination.absolutePath}' " +
                        "because a new version is available on the server..."
            )
            connection.downloadZipFile(zipDownloadDestination)
            setLastModifiedOnDisk(lastModifiedOnServer)
        }
    }

    private fun URLConnection.downloadZipFile(destinationFile: File) {
        destinationFile.parentFile.toPath().createDirectories()
        getInputStream().use { downloadStream ->
            destinationFile.outputStream().use { outputStream ->
                downloadStream.copyTo(outputStream)
            }
        }
    }

    private fun getLastModifiedFromFile() = if (!lastUpdatedFile.exists()) null
    else lastUpdatedFile.readText()
        .let { ZonedDateTime.parse(it, lastModifiedFormatter) }

    private fun setLastModifiedOnDisk(timestamp: ZonedDateTime) {
        lastModifiedFormatter.format(timestamp)
            .let { lastUpdatedFile.writeText(it) }
    }

    private fun URLConnection.getLastModifiedDate(): ZonedDateTime {
        val lastModifiedString = getHeaderField("Last-Modified")
        return ZonedDateTime.parse(lastModifiedString, lastModifiedFormatter)
    }
}
