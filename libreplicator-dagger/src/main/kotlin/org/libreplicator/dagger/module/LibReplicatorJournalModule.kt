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

package org.libreplicator.dagger.module

import com.google.common.io.Files
import dagger.Binds
import dagger.Module
import dagger.Provides
import org.libreplicator.interactor.api.journal.JournalService
import org.libreplicator.journal.DefaultJournalService
import org.libreplicator.journal.reader.DefaultJournalEntryReader
import org.libreplicator.journal.reader.FileReader
import org.libreplicator.journal.reader.JournalEntryReader
import org.libreplicator.journal.writer.DefaultJournalEntryWriter
import org.libreplicator.journal.writer.FileWriter
import org.libreplicator.journal.writer.JournalEntryWriter
import javax.inject.Named

@Module
abstract class LibReplicatorJournalModule {
    companion object {

    }

    @Binds abstract fun bindJournalService(defaultJournalService: DefaultJournalService): JournalService
    @Binds abstract fun bindJournalEntryWriter(defaultJournalEntryWriter: DefaultJournalEntryWriter): JournalEntryWriter
    @Binds abstract fun bindJournalEntryReader(defaultJournalEntryReader: DefaultJournalEntryReader): JournalEntryReader
}
