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

package org.libreplicator.client

import org.libreplicator.api.ReplicatorNode
import org.libreplicator.client.api.ReplicatorClient
import org.libreplicator.httpclient.api.HttpClient
import org.libreplicator.crypto.api.Cipher
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.model.ReplicatorMessage
import javax.inject.Inject
import javax.inject.Provider

class DefaultReplicatorClient @Inject constructor(
        private val jsonMapper: JsonMapper,
        private val cipher: Cipher,
        private val httpClientProvider: Provider<HttpClient>) : ReplicatorClient {

    private companion object {
        private val SYNC_PATH = "/sync"
    }

    private lateinit var httpClient: HttpClient

    override fun initialize() {
        httpClient = httpClientProvider.get()
    }

    override fun synchronizeWithNode(remoteNode: ReplicatorNode, message: ReplicatorMessage) {
        httpClient.post(remoteNode.url, remoteNode.port, SYNC_PATH, cipher.encrypt(jsonMapper.write(message)))
    }

    override fun close() = httpClient.close()
}