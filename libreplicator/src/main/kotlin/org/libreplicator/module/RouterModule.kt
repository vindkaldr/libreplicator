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

package org.libreplicator.module

import dagger.Module
import dagger.Provides
import org.libreplicator.core.client.ReplicatorClient
import org.libreplicator.core.router.DefaultMessageRouter
import org.libreplicator.core.router.MessageRouter
import org.libreplicator.core.server.ReplicatorServer
import org.libreplicator.json.api.JsonMapper
import javax.inject.Singleton

@Module
class RouterModule {
    @Provides @Singleton
    fun bindMessageRouter(
        jsonMapper: JsonMapper,
        replicatorClient: ReplicatorClient,
        replicatorServer: ReplicatorServer
    ): MessageRouter {
        return DefaultMessageRouter(jsonMapper, replicatorClient, replicatorServer)
    }
}
