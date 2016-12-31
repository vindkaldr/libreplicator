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

package org.libreplicator.guice.module

import com.google.common.io.Files
import com.google.inject.PrivateModule
import com.google.inject.Singleton
import com.google.inject.name.Names
import org.libreplicator.interactor.api.journal.JournalService
import org.libreplicator.journal.DefaultJournalService
import org.libreplicator.journal.reader.DefaultJournalEntryReader
import org.libreplicator.journal.reader.FileReader
import org.libreplicator.journal.reader.JournalEntryReader
import org.libreplicator.journal.writer.DefaultJournalEntryWriter
import org.libreplicator.journal.writer.FileWriter
import org.libreplicator.journal.writer.JournalEntryWriter
import java.io.File

class LibReplicatorJournalModule : PrivateModule() {
    override fun configure() {
        expose(JournalService::class.java)
        bind(JournalService::class.java).to(DefaultJournalService::class.java).`in`(Singleton::class.java)

        bind(JournalEntryWriter::class.java).to(DefaultJournalEntryWriter::class.java).`in`(Singleton::class.java)
        bind(FileWriter::class.java)

        bind(JournalEntryReader::class.java).to(DefaultJournalEntryReader::class.java).`in`(Singleton::class.java)
        bind(FileReader::class.java)

        bind(File::class.java).annotatedWith(Names.named("journalDirectory")).toInstance(Files.createTempDir())
    }
}
