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

package org.libreplicator.component

import dagger.Component
import org.libreplicator.boundary.module.BoundaryModule
import org.libreplicator.client.module.ClientModule
import org.libreplicator.crypto.module.CryptoModule
import org.libreplicator.gateway.module.test.FakeGatewayModule
import org.libreplicator.httpclient.module.HttpClientModule
import org.libreplicator.httpserver.module.HttpServerModule
import org.libreplicator.interactor.module.InteractorModule
import org.libreplicator.journal.module.JournalModule
import org.libreplicator.json.module.JsonModule
import org.libreplicator.locator.module.test.FakeLocatorModule
import org.libreplicator.server.module.ServerModule
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(
        BoundaryModule::class,
        ClientModule::class,
        CryptoModule::class,
        FakeGatewayModule::class,
        HttpClientModule::class,
        HttpServerModule::class,
        InteractorModule::class,
        JournalModule::class,
        JsonModule::class,
        FakeLocatorModule::class,
        ServerModule::class))
interface TestComponent : ProductionComponent
