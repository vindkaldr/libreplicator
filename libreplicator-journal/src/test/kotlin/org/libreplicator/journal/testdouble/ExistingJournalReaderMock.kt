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

package org.libreplicator.journal.testdouble

import org.junit.Assert
import org.libreplicator.journal.file.FileHandler
import java.nio.file.Path

class ExistingJournalReaderMock(private val journal: Path, private val content: String) : FileHandler {
    override fun createDirectory(parentPath: Path, directoryName: String): Path {
        return parentPath.resolve(directoryName)
    }

    override fun readFirstLine(path: Path): String {
        if (journal != path) {
            Assert.fail("Unexpected call!")
        }
        return content
    }

    override fun write(path: Path, line: String) {
    }

    override fun move(source: Path, destination: Path) {
    }
}
