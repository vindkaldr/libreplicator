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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import org.eclipse.jetty.http.HttpStatus
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.libreplicator.client.api.ReplicatorClient
import org.libreplicator.client.testdouble.CipherStub
import org.libreplicator.client.testdouble.JsonMapperStub
import org.libreplicator.crypto.api.Cipher
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.model.EventNode
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.TimeTable

class DefaultReplicatorClientIntegrationTest {
    private companion object {
        private val MESSAGE = ReplicatorMessage("nodeId", listOf(), TimeTable())
        private val SERIALIZED_MESSAGE = "{\"nodeId\":\"nodeId\",\"eventLogs\":[],\"timeTable\":[]}"
        private val ENCRYPTED_MESSAGE = "a99a56aea35c30206f31da9a0164641855dbe332b34f9197d327"

        private val REMOTE_NODE = EventNode("remoteNode", "localhost", 12346)
        private val SYNC_PATH = "/sync"
    }

    private lateinit var wireMockServer: WireMockServer

    private val jsonMapperStub: JsonMapper = JsonMapperStub(message = MESSAGE, deserializedMessage = SERIALIZED_MESSAGE)
    private val cipherStub: Cipher = CipherStub(message = SERIALIZED_MESSAGE, encryptedMessage = ENCRYPTED_MESSAGE)

    private lateinit var replicatorClient: ReplicatorClient

    @Before
    fun setUp() {
        wireMockServer = WireMockServer(options().port(REMOTE_NODE.port))
        wireMockServer.start()

        replicatorClient = DefaultReplicatorClient(jsonMapperStub, cipherStub)
    }

    @After
    fun tearDown() {
        wireMockServer.stop()
    }

    @Test
    fun replicatorClient_shouldSendMessage_inCorrectForm() {
        wireMockServer.stubFor(post(urlPathEqualTo(SYNC_PATH))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NO_CONTENT_204)))

        replicatorClient.synchronizeWithNode(REMOTE_NODE, MESSAGE)

        wireMockServer.verify(postRequestedFor(urlPathMatching(SYNC_PATH))
                .withRequestBody(equalTo(ENCRYPTED_MESSAGE)))
    }
}
