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

package org.libreplicator.httpserver

import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.libreplicator.httpserver.api.HttpServer
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.servlet.http.HttpServlet

class DefaultHttpServer @Inject constructor() : HttpServer {
    private companion object {
        private val logger = LoggerFactory.getLogger(DefaultHttpServer::class.java)
    }

    private val coroutineContext = newSingleThreadContext("")
    private lateinit var server: Server

    override suspend fun start(port: Int, path: String, httpServlet: HttpServlet) {
        logger.trace("Starting http server..")
        server = Server(port)

        val publicContext = ServletContextHandler()

        val syncEndpointHolder = ServletHolder(httpServlet)
        publicContext.addServlet(syncEndpointHolder, path)
        syncEndpointHolder.isAsyncSupported = true

        val handlerCollection = HandlerCollection()
        handlerCollection.addHandler(publicContext)

        server.handler = publicContext

        launch(coroutineContext) {
            server.start()
        }.join()
        logger.trace("Started http server")
    }

    override suspend fun stop() {
        logger.trace("Stopping http server..")

        launch(coroutineContext) {
            server.stop()
            server.join()
        }.join()
        logger.trace("Stopped http server")
    }
}
