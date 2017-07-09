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

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.libreplicator.httpserver.api.HttpServer
import javax.inject.Inject
import javax.servlet.http.HttpServlet

class DefaultHttpServer @Inject constructor() : HttpServer {
    private lateinit var server: Server

    override fun startAndWaitUntilStarted(port: Int, path: String, httpServlet: HttpServlet) {
        server = Server(port)

        val publicContext = ServletContextHandler()

        val syncEndpointHolder = ServletHolder(httpServlet)
        publicContext.addServlet(syncEndpointHolder, path)

        val handlerCollection = HandlerCollection()
        handlerCollection.addHandler(publicContext)

        server.handler = handlerCollection

        server.startAndWaitUntilStarted()
    }

    override fun stopAndWaitUntilStopped() {
        server.stopAndWaitUntilStopped()
    }
}
