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

package org.libreplicator.core.transformer

import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.libreplicator.core.router.testdouble.CipherStub
import org.libreplicator.core.router.testdouble.JsonMapperPayloadStub
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.ReplicatorPayload
import org.libreplicator.model.TimeTable

private val payload = ReplicatorPayload("", emptyList(), TimeTable())
private const val serializedPayload = "serializedReplicatorPayload"
private const val encryptedSerializedPayload = "encryptedSerializedReplicatorMessage"

private const val groupId = "groupId"
private val message = ReplicatorMessage(groupId, encryptedSerializedPayload)

private val jsonMapperStub = JsonMapperPayloadStub(objectToWrite = payload, stringToRead = serializedPayload)
private val cipherStub = CipherStub(contentToEncrypt = serializedPayload, contentToDecrypt = encryptedSerializedPayload)

class DefaultPayloadWrapperTest {
    @Test
    fun `wrap writes and encrypts payload with group id`() {
        val wrapper: PayloadWrapper = DefaultPayloadWrapper(groupId, jsonMapperStub, cipherStub)
        assertThat(wrapper.wrap(payload), equalTo(message))
    }

    @Test
    fun `unwrap decrypts and reads payload`() {
        val wrapper: PayloadWrapper = DefaultPayloadWrapper(groupId, jsonMapperStub, cipherStub)
        assertThat(wrapper.unwrap(message), equalTo(payload))
    }
}
