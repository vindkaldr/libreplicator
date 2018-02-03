/*
 *     Copyright (C) 2016  Mihály Szabó
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
import java.nio.file.Paths

class ErroneousJournalReaderMock(
        private val journal: Path,
        private val exceptionToThrow: Throwable) : FileHandler {

    override fun createDirectory(parentPath: Path, directoryName: String): Path {
        return Paths.get("")
    }

    override fun readFirstLine(path: Path): String {
        if (journal != path) {
            Assert.fail("Unexpected call!")
        }
        throw exceptionToThrow
    }

    override fun write(path: Path, line: String) {
    }

    override fun move(source: Path, destination: Path) {
    }
}
