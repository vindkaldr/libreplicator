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

import com.google.inject.PrivateModule
import org.libreplicator.api.LocalEventLogFactory
import org.libreplicator.api.ReplicatorNodeFactory
import org.libreplicator.model.factory.DefaultLocalEventLogFactory
import org.libreplicator.model.factory.DefaultReplicatorNodeFactory

class LibReplicatorModelModule : PrivateModule() {
    override fun configure() {
        expose(LocalEventLogFactory::class.java)
        bind(LocalEventLogFactory::class.java).to(DefaultLocalEventLogFactory::class.java)

        expose(ReplicatorNodeFactory::class.java)
        bind(ReplicatorNodeFactory::class.java).to(DefaultReplicatorNodeFactory::class.java)
    }
}
