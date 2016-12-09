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

package hu.dreamsequencer.replicator.boundary.module

import com.google.inject.PrivateModule
import com.google.inject.assistedinject.FactoryModuleBuilder
import hu.dreamsequencer.replicator.api.Replicator
import hu.dreamsequencer.replicator.api.ReplicatorFactory
import hu.dreamsequencer.replicator.boundary.DefaultReplicator

class ReplicatorBoundaryModule : PrivateModule() {
    override fun configure() {
        expose(ReplicatorFactory::class.java)
        install(FactoryModuleBuilder()
                .implement(Replicator::class.java, DefaultReplicator::class.java)
                .build(ReplicatorFactory::class.java))
    }
}
