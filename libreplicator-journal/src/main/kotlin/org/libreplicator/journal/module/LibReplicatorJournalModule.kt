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

package org.libreplicator.journal.module

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.crypto.api.Cipher
import org.libreplicator.journal.DefaultReplicatorStateJournal
import org.libreplicator.journal.file.DefaultFileHandler
import org.libreplicator.journal.file.FileHandler
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.model.ReplicatorState
import javax.inject.Singleton

@Module
class LibReplicatorJournalModule (
        private val journalSettings: LibReplicatorJournalSettings,
        private val localNode: ReplicatorNode,
        private val remoteNodes: List<ReplicatorNode>) {

    @Provides
    fun bindFileHandler(): FileHandler {
        return DefaultFileHandler()
    }

    @Provides @Singleton
    fun provideReplicatorState(fileHandler: FileHandler, jsonMapper: JsonMapper, cipher: Cipher): ReplicatorState = runBlocking {
        if (!journalSettings.isJournalingEnabled) {
            return@runBlocking ReplicatorState()
        }

        val replicatorStateJournal = DefaultReplicatorStateJournal(fileHandler, jsonMapper, cipher,
                journalSettings.directoryOfJournals, localNode, remoteNodes)
        val replicatorState = replicatorStateJournal.getReplicatorState()

        replicatorState.subscribe(replicatorStateJournal)

        return@runBlocking replicatorState
    }
}
