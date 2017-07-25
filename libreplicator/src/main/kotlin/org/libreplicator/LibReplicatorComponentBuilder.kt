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

import org.libreplicator.api.LocalNode
import org.libreplicator.api.RemoteNode
import org.libreplicator.component.DaggerLibReplicatorComponent
import org.libreplicator.component.LibReplicatorComponent
import org.libreplicator.crypto.module.LibReplicatorCryptoModule
import org.libreplicator.journal.module.LibReplicatorJournalModule
import org.libreplicator.server.module.LibReplicatorServerModule

class LibReplicatorComponentBuilder(private val settings: LibReplicatorSettings) {
    fun build(localNode: LocalNode, remoteNodes: List<RemoteNode>,
            block: DaggerLibReplicatorComponent.Builder.() -> Unit = {}): LibReplicatorComponent {
        return DaggerLibReplicatorComponent.builder()
                .libReplicatorCryptoModule(LibReplicatorCryptoModule(settings.cryptoSettings))
                .libReplicatorJournalModule(LibReplicatorJournalModule(settings.journalSettings, localNode, remoteNodes))
                .libReplicatorServerModule(LibReplicatorServerModule(localNode))
                .apply(block)
                .build()
    }
}
