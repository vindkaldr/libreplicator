package hu.dreamsequencer.replicator.interactor

import hu.dreamsequencer.replicator.api.RemoteEventLogObserver
import hu.dreamsequencer.replicator.api.ReplicatorNode
import hu.dreamsequencer.replicator.interactor.api.LogDispatcher
import hu.dreamsequencer.replicator.interactor.api.LogDispatcherFactory
import hu.dreamsequencer.replicator.interactor.api.LogRouterFactory
import javax.inject.Inject
import javax.inject.Provider

class DefaultLogDispatcherFactory
@Inject constructor(private val logRouterFactoryProvider: Provider<LogRouterFactory>) : LogDispatcherFactory {
    override fun create(localNode: ReplicatorNode,
                        remoteNodes: List<ReplicatorNode>,
                        remoteEventLogObserver: RemoteEventLogObserver): LogDispatcher =
            DefaultLogDispatcher(logRouterFactoryProvider.get(), localNode, remoteNodes, remoteEventLogObserver)
}
