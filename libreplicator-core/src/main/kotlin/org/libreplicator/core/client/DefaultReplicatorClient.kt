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

package org.libreplicator.core.client

import org.libreplicator.api.RemoteNode
import org.libreplicator.core.client.api.ReplicatorClient
import org.libreplicator.httpclient.api.HttpClient
import org.libreplicator.locator.api.NodeLocator
import javax.inject.Inject
import javax.inject.Provider

class DefaultReplicatorClient @Inject constructor(
        private val nodeLocator: NodeLocator,
        private val httpClientProvider: Provider<HttpClient>
) : ReplicatorClient {
    private companion object {
        private val SYNC_PATH = "/sync"
    }

    private lateinit var httpClient: HttpClient

    override fun initialize() {
        httpClient = httpClientProvider.get()
    }

    override fun synchronizeWithNode(remoteNode: RemoteNode, message: String) {
        val node = resolveNode(remoteNode)
        if (node != null) {
            val uri = httpClient.createUri(node.hostname, node.port,
                SYNC_PATH
            )
            httpClient.post(uri, message)
        }
    }

    private fun resolveNode(remoteNode: RemoteNode): RemoteNode? {
        if (remoteNode.hostname == "" || remoteNode.port == 0) {
            return nodeLocator.getNode(remoteNode.nodeId)
        }
        return remoteNode
    }

    override fun close() = httpClient.close()
}
