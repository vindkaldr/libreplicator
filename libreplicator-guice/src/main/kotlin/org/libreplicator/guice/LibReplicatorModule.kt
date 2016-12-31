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

package org.libreplicator.guice

import com.google.inject.AbstractModule
import org.libreplicator.guice.module.LibReplicatorBoundaryModule
import org.libreplicator.guice.module.LibReplicatorInteractorModule
import org.libreplicator.guice.module.LibReplicatorJournalModule
import org.libreplicator.guice.module.LibReplicatorJsonModule
import org.libreplicator.guice.module.LibReplicatorModelModule
import org.libreplicator.guice.module.LibReplicatorNetworkModule

class LibReplicatorModule : AbstractModule() {
    override fun configure() {
        install(LibReplicatorBoundaryModule())
        install(LibReplicatorInteractorModule())
        install(LibReplicatorJournalModule())
        install(LibReplicatorJsonModule())
        install(LibReplicatorModelModule())
        install(LibReplicatorNetworkModule())
    }
}
