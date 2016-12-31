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

package org.libreplicator.journal.writer

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class FileWriter {
    fun exists(path: Path): Boolean = Files.exists(path)
    fun write(path: Path, line: String): Path = Files.write(path, listOf(line))
    fun append(path: Path, line: String): Path = Files.write(path, listOf(line), StandardOpenOption.APPEND)
    fun delete(path: Path) = Files.delete(path)
}
