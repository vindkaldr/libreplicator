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

package org.libreplicator.core.journal.testdouble

import org.junit.Assert
import org.libreplicator.core.journal.file.FileHandler
import java.nio.file.Path

class JournalHandlerMock(
        private val journalsDirectory: Path,
        private val journalDirectoryName: String,
        private val journalDirectory: Path) : FileHandler {

    private var observedJournalsDirectory: Path? = null
    private var observedJournalDirectoryName: String? = null

    private var writtenJournal: Path? = null
    private var writtenJournalContent: String? = null

    private var movedJournalSource: Path? = null
    private var movedJournalDestination: Path? = null

    override fun createDirectory(parentPath: Path, directoryName: String): Path {
        if (journalsDirectory != parentPath || journalDirectoryName != directoryName) {
            Assert.fail("Unexpected call!")
        }
        observedJournalsDirectory = parentPath
        observedJournalDirectoryName = directoryName
        return journalDirectory
    }

    override fun readFirstLine(path: Path): String {
        return ""
    }

    override fun write(path: Path, line: String) {
        if (writtenJournal != null && writtenJournalContent != null) {
            Assert.fail("Unexpected call!")
        }
        writtenJournal = path
        writtenJournalContent = line
    }

    override fun move(source: Path, destination: Path) {
        if (movedJournalSource != null || movedJournalDestination != null) {
            Assert.fail("Unexpected call!")
        }
        movedJournalSource = source
        movedJournalDestination = destination
    }

    fun createdDirectory(journalsDirectory: Path, journalDirectoryName: String): Boolean {
        return observedJournalsDirectory == journalsDirectory && observedJournalDirectoryName == journalDirectoryName
    }

    fun wroteJournal(journal: Path, content: String): Boolean {
        return writtenJournal == journal && writtenJournalContent == content
    }

    fun movedJournal(journalSource: Path, journalDestination: Path): Boolean {
        return movedJournalSource == journalSource && movedJournalDestination == journalDestination
    }
}
