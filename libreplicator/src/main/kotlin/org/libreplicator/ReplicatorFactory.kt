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

package org.libreplicator

import org.libreplicator.api.LocalNode
import org.libreplicator.api.RemoteNode
import org.libreplicator.api.Replicator
import org.libreplicator.component.DaggerLibReplicatorComponent
import org.libreplicator.component.replicator.DaggerReplicatorComponent
import org.libreplicator.module.ServerModule
import org.libreplicator.module.replicator.CoreModule
import org.libreplicator.module.replicator.CryptoModule
import org.libreplicator.module.replicator.JournalModule

class ReplicatorFactory(private val localNode: LocalNode) {
    private val libReplicatorComponent = DaggerLibReplicatorComponent.builder()
        .serverModule(ServerModule(localNode))
        .build()

    fun create(
        groupId: String,
        remoteNodes: List<RemoteNode>,
        settings: ReplicatorSettings = ReplicatorSettings()
    ): Replicator {
        return DaggerReplicatorComponent.builder()
            .libReplicatorComponent(libReplicatorComponent)
            .coreModule(CoreModule(groupId))
            .cryptoModule(CryptoModule(settings.cryptoSettings))
            .journalModule(JournalModule(settings.journalSettings, localNode, remoteNodes))
            .build()
            .replicator()
    }
}
