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

package org.libreplicator.component

import dagger.Component
import org.libreplicator.client.api.ReplicatorClient
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.module.ClientModule
import org.libreplicator.module.GatewayModule
import org.libreplicator.module.HttpClientModule
import org.libreplicator.module.HttpServerModule
import org.libreplicator.module.JsonModule
import org.libreplicator.module.LocatorModule
import org.libreplicator.module.ServerModule
import org.libreplicator.server.api.ReplicatorServer
import javax.inject.Singleton

@Singleton
@Component(modules = [
    ClientModule::class,
    GatewayModule::class,
    HttpClientModule::class,
    HttpServerModule::class,
    JsonModule::class,
    LocatorModule::class,
    ServerModule::class
])
interface LibReplicatorComponent {
    fun replicatorClient(): ReplicatorClient
    fun replicatorServer(): ReplicatorServer
    fun jsonMapper(): JsonMapper
}
