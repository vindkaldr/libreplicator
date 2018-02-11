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

package org.libreplicator.core.router.testdouble

import org.junit.Assert.fail
import org.libreplicator.api.RemoteNode
import org.libreplicator.core.client.ReplicatorClient

class ReplicatorClientStub : ReplicatorClient {
    private var observedInitialize: Int = 0

    private var observedRemoteNode: RemoteNode? = null
    private var observedMessage: String? = null

    private var observedClose: Int = 0

    override fun initialize() {
        observedInitialize += 1
    }

    override fun synchronizeWithNode(remoteNode: RemoteNode, message: String) {
        if (observedRemoteNode != null && observedMessage != null) {
            fail("Unexpected call!")
        }
        observedRemoteNode = remoteNode
        observedMessage = message
    }

    override fun close() {
        observedClose += 1
    }

    fun isInitialized() = observedInitialize > 0
    fun isInitializedOnce() = observedInitialize == 1

    fun sentMessage(remoteNode: RemoteNode, message: String): Boolean {
        return observedRemoteNode == remoteNode && observedMessage == message
    }

    fun isClosed() = observedClose > 0
    fun isClosedOnce() = observedClose == 1
}
