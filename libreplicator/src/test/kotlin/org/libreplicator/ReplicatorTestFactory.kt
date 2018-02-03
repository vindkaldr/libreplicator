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
import org.libreplicator.component.DaggerTestComponent
import org.libreplicator.module.CryptoModule
import org.libreplicator.module.JournalModule
import org.libreplicator.module.ServerModule

class ReplicatorTestFactory(private val settings: ReplicatorSettings = ReplicatorSettings()) {
    fun create(localNode: LocalNode, remoteNodes: List<RemoteNode>): Replicator {
        return DaggerTestComponent.builder()
                .cryptoModule(CryptoModule(settings.cryptoSettings))
                .journalModule(JournalModule(settings.journalSettings, localNode, remoteNodes))
                .serverModule(ServerModule(localNode))
                .build()
                .getReplicator()
    }
}
