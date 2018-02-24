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

package org.libreplicator.httpclient

import org.apache.http.NoHttpResponseException
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.conn.HttpHostConnectException
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.libreplicator.httpclient.api.HttpClient
import org.libreplicator.log.api.trace
import org.libreplicator.log.api.warn
import java.net.SocketTimeoutException
import java.net.URI
import javax.inject.Inject

private const val HTTP_SCHEME = "http"
private const val SOCKET_TIMEOUT = 1000

class DefaultHttpClient @Inject constructor() : HttpClient {
    private val httpClient: CloseableHttpClient = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setSocketTimeout(SOCKET_TIMEOUT)
                    .build())
            .build()

    override fun post(uri: URI, content: String) {
        try {
            httpClient.execute(createHttpPostRequest(uri, content))
            trace("Connected to remote node")
        }
        catch (e: HttpHostConnectException) {
            warn("Failed to connect to remote node!")
        }
        catch (e: NoHttpResponseException) {
            warn("Failed to reuse connection!")
        }
        catch (e: SocketTimeoutException) {
            warn("Failed to connect to remote node!")
        }
    }

    override fun createUri(hostname: String, port: Int, path: String): URI {
        return URIBuilder()
                .setScheme(HTTP_SCHEME)
                .setHost(hostname)
                .setPort(port)
                .setPath(path)
                .build()
    }

    private fun createHttpPostRequest(uri: URI, content: String): HttpPost {
        val httpPost = HttpPost(uri)
        httpPost.entity = StringEntity(content)
        return httpPost
    }

    override fun close() {
        httpClient.close()
    }
}
