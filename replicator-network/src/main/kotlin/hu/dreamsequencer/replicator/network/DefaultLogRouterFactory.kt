package hu.dreamsequencer.replicator.network

import hu.dreamsequencer.replicator.api.ReplicatorNode
import hu.dreamsequencer.replicator.interactor.api.LogDispatcher
import hu.dreamsequencer.replicator.interactor.api.LogRouter
import hu.dreamsequencer.replicator.interactor.api.LogRouterFactory
import hu.dreamsequencer.replicator.json.api.JsonMapper
import javax.inject.Inject

class DefaultLogRouterFactory @Inject constructor(private val jsonMapper: JsonMapper): LogRouterFactory {
    override fun create(localNode: ReplicatorNode, logDispatcher: LogDispatcher): LogRouter =
            DefaultLogRouter(jsonMapper, localNode, logDispatcher)
}
