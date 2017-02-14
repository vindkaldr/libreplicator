/*
 *     Copyright (C) 2016  Mihaly Szabo <szmihaly91@gmail.com/>
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplicator.journal

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

class FileHandler {
    fun readFirstLine(path: Path): String {
        val allLines = Files.readAllLines(path)

        if (allLines.isEmpty()) {
            return "";
        }
        return allLines.first()
    }

    fun write(path: Path, line: String) {
        Files.write(path, listOf(line), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.SYNC)
    }

    fun createDirectory(parentPath: Path, directoryName: String): Path {
        return createDirectory(parentPath.resolve(directoryName))
    }

    private fun createDirectory(path: Path): Path {
        path.toFile().mkdirs()
        return path
    }

    fun isExists(path: Path): Boolean {
        return path.toFile().exists()
    }

    fun createFile(parentPath: Path, path: Path): Path {
        val filePath = parentPath.resolve(path)
        filePath.toFile().createNewFile()
        return filePath
    }

    fun move(source: Path, destination: Path) {
        Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE)
    }
}
