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

package org.libreplicator.server

import kotlinx.coroutines.experimental.runBlocking
import org.libreplicator.api.Observer
import org.libreplicator.crypto.api.Cipher
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.model.ReplicatorMessage
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ReplicatorSyncServlet(
        private val jsonMapper: JsonMapper,
        private val cipher: Cipher,
        private val messageObserver: Observer<ReplicatorMessage>) : HttpServlet() {

    private companion object {
        private val logger = LoggerFactory.getLogger(ReplicatorSyncServlet::class.java)
    }

    override fun doPost(req: HttpServletRequest?, resp: HttpServletResponse?) = runBlocking {
        try {
            val requestBody = req?.reader?.readText() ?: ""
            messageObserver.observe(jsonMapper.read(cipher.decrypt(requestBody), ReplicatorMessage::class))
        }
        catch (e: JsonReadException) {
            logger.warn("Failed to deserialize message!")
        }
    }
}
