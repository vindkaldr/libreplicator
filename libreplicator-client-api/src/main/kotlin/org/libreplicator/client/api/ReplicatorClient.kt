package org.libreplicator.client.api

import org.libreplicator.api.ReplicatorNode
import org.libreplicator.model.ReplicatorMessage
import java.io.Closeable

interface ReplicatorClient : Closeable {
    fun synchronizeWithNode(remoteNode: ReplicatorNode, message: ReplicatorMessage)
}
