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
import dagger.Module
import dagger.Provides
import org.libreplicator.journal.reader.FileReader
import org.libreplicator.journal.writer.FileWriter
import javax.inject.Named

@Module
class LibReplicatorJournalProviderModule {
    @Provides fun provideFileWriter() = FileWriter()
    @Provides fun provideFileReader() = FileReader()
    @Provides @Named("journalDirectory") fun provideJournalDirectory() = Files.createTempDir()
}
