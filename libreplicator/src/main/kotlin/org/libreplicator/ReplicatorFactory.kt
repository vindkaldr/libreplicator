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

package org.libreplicator

import com.google.common.io.Files
import org.libreplicator.api.Replicator
import org.libreplicator.api.ReplicatorFactory
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.boundary.DefaultReplicatorFactory
import org.libreplicator.interactor.DefaultEventLogHandler
import org.libreplicator.interactor.DefaultLogDispatcherFactory
import org.libreplicator.journal.DefaultJournalService
import org.libreplicator.journal.reader.DefaultJournalEntryReader
import org.libreplicator.journal.reader.FileReader
import org.libreplicator.journal.writer.DefaultJournalEntryWriter
import org.libreplicator.journal.writer.FileWriter
import org.libreplicator.json.DefaultJsonMapper
import org.libreplicator.network.DefaultLogRouterFactory

class ReplicatorFactory : ReplicatorFactory {
    override fun create(localNode: ReplicatorNode, remoteNodes: List<ReplicatorNode>): Replicator {
        val journalDirectory = Files.createTempDir()
        val journalEntryReader = DefaultJournalEntryReader(journalDirectory, FileReader(), DefaultJsonMapper())
        val journalEntryWriter = DefaultJournalEntryWriter(journalDirectory, journalEntryReader, FileWriter(), DefaultJsonMapper())
        val journalService = DefaultJournalService(journalEntryWriter)

        val logRouterFactory = DefaultLogRouterFactory(DefaultJsonMapper())
        val eventLogHandler = DefaultEventLogHandler()
        val logDispatcherFactory = DefaultLogDispatcherFactory(logRouterFactory, eventLogHandler, journalService)
        return DefaultReplicatorFactory(logDispatcherFactory).create(localNode, remoteNodes)
    }
}
