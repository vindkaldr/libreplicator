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

package org.libreplicator.httpserver

import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import kotlinx.coroutines.experimental.runBlocking
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.libreplicator.api.Observer
import org.libreplicator.httpserver.api.HttpServer
import org.slf4j.LoggerFactory
import javax.inject.Inject

class DefaultHttpServer @Inject constructor() : HttpServer {
    private companion object {
        private val logger = LoggerFactory.getLogger(DefaultHttpServer::class.java)
    }

    private val coroutineContext = newSingleThreadContext("")
    private lateinit var server: Http4kServer

    override suspend fun start(port: Int, path: String, messageObserver: Observer<String>) {
        launch(coroutineContext) {
            server = routes(path bind Method.POST to { request: Request -> runBlocking {
                val message = request.body.payload.array().toString(Charsets.UTF_8)
                messageObserver.observe(message)
                Response(Status.OK)
            }}).asServer(Jetty(port)).start()
        }.join()
        logger.trace("Started http server")
    }

    override suspend fun stop() {
        launch(coroutineContext) {
            logger.trace("Stopping http server..")
            server.stop()
            logger.trace("Stopped http server")
        }.join()
    }
}
