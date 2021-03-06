/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2016 - 2021 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.renderer.ultralight.theme

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.extractZip
import net.ccbluex.liquidbounce.utils.resource
import java.io.File
import java.nio.file.StandardWatchEventKinds.*

// TODO: make better

object ThemeManager {

    val themesFolder = File(LiquidBounce.configSystem.rootFolder, "themes")
    val defaultTheme = Theme.default()

}

class Theme(val name: String) {

    internal val themeFolder = File(ThemeManager.themesFolder, name)

    val exists: Boolean
        get() = themeFolder.exists()

    companion object {

        fun default() = Theme("default").apply {
            if (exists) {
                themeFolder.delete()
            }

            extractZip(resource("/assets/liquidbounce/default_theme.zip"), themeFolder)
            themeFolder.deleteOnExit()
        }

    }

    fun page(name: String) = Page(this, name)

}

class Page(theme: Theme, val name: String) {

    private val pageFolder = File(theme.themeFolder, name)
    private val htmlFile = File(pageFolder, "index.html")

    val viewableFile: String
        get() = htmlFile.toURI().toString()

    private val watcher by lazy {
        val path = pageFolder.toPath()
        val watchService = path.fileSystem.newWatchService()
        path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
        watchService
    }

    fun hasUpdate(): Boolean {
        val watchKey = watcher.poll()
        val shouldUpdate = watchKey?.pollEvents()?.isNotEmpty() == true
        watchKey?.reset()
        return shouldUpdate
    }

    fun close() {
        watcher.close()
    }

    override fun toString() = "Page($name, $viewableFile)"

}
