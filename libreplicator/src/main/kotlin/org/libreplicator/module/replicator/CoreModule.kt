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

package org.libreplicator.module.replicator

import dagger.Module
import dagger.Provides
import org.libreplicator.api.Replicator
import org.libreplicator.component.replicator.ReplicatorScope
import org.libreplicator.core.replicator.DefaultReplicator
import org.libreplicator.core.router.api.MessageRouter
import org.libreplicator.core.interactor.DefaultStateInteractor
import org.libreplicator.core.interactor.api.StateInteractor
import org.libreplicator.core.wrapper.DefaultPayloadWrapper
import org.libreplicator.core.wrapper.api.PayloadWrapper
import org.libreplicator.crypto.api.Cipher
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.model.ReplicatorState

@Module
class CoreModule(private val groupId: String) {
    @Provides @ReplicatorScope
    fun bindReplicator(
        stateInteractor: StateInteractor,
        payloadWrapper: PayloadWrapper,
        messageRouter: MessageRouter
    ): Replicator {
        return DefaultReplicator(groupId, stateInteractor, payloadWrapper, messageRouter)
    }
    @Provides @ReplicatorScope
    fun bindStateInteractor(replicatorState: ReplicatorState): StateInteractor {
        return DefaultStateInteractor(replicatorState)
    }

    @Provides @ReplicatorScope
    fun bindPayloadWrapper(jsonMapper: JsonMapper, cipher: Cipher): PayloadWrapper {
        return DefaultPayloadWrapper(groupId, jsonMapper, cipher)
    }
}
