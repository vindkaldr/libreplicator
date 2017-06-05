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

import org.apache.http.NoHttpResponseException
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.conn.HttpHostConnectException
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.client.api.ReplicatorClient
import org.libreplicator.crypto.api.Cipher
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.model.ReplicatorMessage
import org.slf4j.LoggerFactory
import java.net.SocketTimeoutException
import javax.inject.Inject

class DefaultReplicatorClient @Inject constructor(
        private val jsonMapper: JsonMapper,
        private val cipher: Cipher) : ReplicatorClient {

    private companion object {
        private val logger = LoggerFactory.getLogger(DefaultReplicatorClient::class.java)
        private val SYNC_PATH = "/sync"
    }

    private val httpClient: CloseableHttpClient = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setSocketTimeout(1000)
                    .build())
            .build()

    override fun synchronizeWithNode(remoteNode: ReplicatorNode, message: ReplicatorMessage) {
        try {
            serializeAndSendMessage(remoteNode, message)
        }
        catch (e: HttpHostConnectException) {
            logger.info("Failed to connect to remote node!")
        }
        catch (e: NoHttpResponseException) {
            logger.warn("Failed to reuse connection!")
        }
        catch (e: SocketTimeoutException) {
            logger.warn("Failed to connect to remote node!")
        }
    }

    private fun serializeAndSendMessage(remoteNode: ReplicatorNode, message: ReplicatorMessage) {
        val httpPostRequest = createHttpPostRequest(remoteNode, SYNC_PATH, message)
        httpClient.execute(httpPostRequest)
    }

    private fun createHttpPostRequest(remoteNode: ReplicatorNode, path: String, message: ReplicatorMessage): HttpPost {
        val httpPost = HttpPost(remoteNode.toUri(path))
        httpPost.entity = StringEntity(cipher.encrypt(jsonMapper.write(message)))
        return httpPost
    }

    override fun close() = httpClient.close()
}
