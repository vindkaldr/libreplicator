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
import java.nio.file.Paths

class JournalDirectoryCreatorMock : FileHandler {
    private var observedJournalsDirectory: Path? = null
    private var observedJournalDirectoryName: String? = null

    override fun createDirectory(parentPath: Path, directoryName: String): Path {
        if (observedJournalsDirectory != null || observedJournalDirectoryName != null) {
            Assert.fail("Unexpected call!")
        }
        observedJournalsDirectory = parentPath
        observedJournalDirectoryName = directoryName
        return Paths.get("")
    }

    override fun readFirstLine(path: Path): String {
        return ""
    }

    override fun write(path: Path, line: String) {
    }

    override fun move(source: Path, destination: Path) {
    }

    fun createdDirectoryWith(journalsDirectory: Path, journalDirectoryName: String): Boolean {
        return observedJournalsDirectory == journalsDirectory && observedJournalDirectoryName == journalDirectoryName
    }
}
