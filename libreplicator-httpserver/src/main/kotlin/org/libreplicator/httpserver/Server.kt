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
import java.lang.Thread.sleep

private const val CHECK_TIMEOUT_IN_MILLIS = 250L

fun Server.startAndWaitUntilStarted() {
    this.start()
    while (!server.isStarted) {
        sleep(CHECK_TIMEOUT_IN_MILLIS)
    }
}

fun Server.stopAndWaitUntilStopped() {
    this.stop()
    while (!server.isStopped) {
        sleep(CHECK_TIMEOUT_IN_MILLIS)
    }
}
