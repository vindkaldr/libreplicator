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

package org.libreplicator.module

import dagger.Module
import dagger.Provides
import org.libreplicator.api.LocalNode
import org.libreplicator.api.RemoteNode
import org.libreplicator.core.locator.DefaultNodeLocator
import org.libreplicator.core.locator.api.NodeLocator
import org.libreplicator.core.locator.api.NodeLocatorSettings
import org.libreplicator.json.api.JsonMapper
import java.lang.Thread.sleep
import javax.inject.Singleton

@Module
class FakeLocatorModule(
    private val localNode: LocalNode,
    private val settings: NodeLocatorSettings
) {
    @Provides @Singleton
    fun provideNodeLocator(jsonMapper: JsonMapper): NodeLocator {
        return BlockingNodeLocator(DefaultNodeLocator(localNode, settings, jsonMapper), settings)
    }

    private class BlockingNodeLocator(
        private val nodeLocator: NodeLocator,
        private val settings: NodeLocatorSettings
    ) : NodeLocator by nodeLocator {
        override fun getNode(nodeId: String): RemoteNode? {
            while (true) {
                val node = nodeLocator.getNode(nodeId)
                if (node != null) return node
                sleep(settings.multicastPeriodInMilliseconds)
            }
        }
    }
}
