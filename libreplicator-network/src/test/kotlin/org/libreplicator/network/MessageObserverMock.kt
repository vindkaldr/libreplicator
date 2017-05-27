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

package org.libreplicator.network

import org.junit.Assert
import org.libreplicator.api.Observer
import org.libreplicator.model.ReplicatorMessage
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class MessageObserverMock private constructor(
        private val expectedMessageCount: Int) : Observer<ReplicatorMessage> {

    companion object {
        private val TIMEOUT_IN_SECONDS = 3L

        fun createWithExpectedMessageCount(expectedMessageCount: Int): MessageObserverMock =
                MessageObserverMock(expectedMessageCount)
    }

    private var observedMessages: List<ReplicatorMessage> = mutableListOf()
    private var semaphore = Semaphore(0)

    override fun observe(observable: ReplicatorMessage) {
        observedMessages += observable
        semaphore.release()
    }

    fun getObservedMessages(): List<ReplicatorMessage> {
        if (!semaphore.tryAcquire(expectedMessageCount, TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)) {
            Assert.fail("Timeout reached!")
        }
        return observedMessages
    }
}
