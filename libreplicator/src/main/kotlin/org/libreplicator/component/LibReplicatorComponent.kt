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

package org.libreplicator.component

import dagger.Component
import org.libreplicator.api.Replicator
import org.libreplicator.boundary.module.LibReplicatorBoundaryModule
import org.libreplicator.client.module.LibReplicatorClientModule
import org.libreplicator.crypto.module.LibReplicatorCryptoModule
import org.libreplicator.interactor.module.LibReplicatorInteractorModule
import org.libreplicator.journal.module.LibReplicatorJournalModule
import org.libreplicator.json.module.LibReplicatorJsonModule
import org.libreplicator.network.module.LibReplicatorNetworkModule
import org.libreplicator.server.module.LibReplicatorServerModule
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(LibReplicatorBoundaryModule::class, LibReplicatorClientModule::class,
        LibReplicatorCryptoModule::class, LibReplicatorInteractorModule::class,
        LibReplicatorJournalModule::class, LibReplicatorJsonModule::class,
        LibReplicatorNetworkModule::class, LibReplicatorServerModule::class))
interface LibReplicatorComponent {
    fun getReplicator(): Replicator
}
