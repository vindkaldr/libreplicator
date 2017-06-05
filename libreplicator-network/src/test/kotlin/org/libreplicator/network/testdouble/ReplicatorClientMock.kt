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

package org.libreplicator.network.testdouble

import org.junit.Assert
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.client.api.ReplicatorClient
import org.libreplicator.model.ReplicatorMessage

class ReplicatorClientMock : ReplicatorClient {
    private var observedRemoteNode: ReplicatorNode? = null
    private var observedMessage: ReplicatorMessage? = null

    private var observedClose: Boolean = false

    override fun send(remoteNode: ReplicatorNode, message: ReplicatorMessage) {
        if (observedRemoteNode != null && observedMessage != null) {
            Assert.fail("Unexpected call!")
        }
        observedRemoteNode = remoteNode
        observedMessage = message
    }

    override fun close() {
        if (observedClose) {
            Assert.fail("Unexpected call!")
        }
        observedClose = true
    }

    fun sentMessage(remoteNode: ReplicatorNode, message: ReplicatorMessage): Boolean {
        return observedRemoteNode == remoteNode && observedMessage == message
    }

    fun wasClosed(): Boolean {
        return observedClose
    }
}
