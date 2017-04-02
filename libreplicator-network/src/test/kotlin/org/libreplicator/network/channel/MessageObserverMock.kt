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

package org.libreplicator.network.channel

import org.junit.Assert
import org.libreplicator.api.Observer
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class MessageObserverMock private constructor(private val expectedMessages: Int) : Observer<String> {
    companion object {
        private val INITIAL_NUMBER_OF_PERMITS = 0
        private val PERMIT_ACQUIRE_TIMEOUT = 1L

        fun createWithExpectedMessages(expectedMessage: Int): MessageObserverMock {
            return MessageObserverMock(expectedMessage)
        }
    }

    private val observedMessages = mutableListOf<String>()
    private val observedMessagesSemaphore = Semaphore(INITIAL_NUMBER_OF_PERMITS)

    override fun observe(observable: String) {
        observedMessages.add(observable)
        observedMessagesSemaphore.release()
    }

    fun getObservedMessages(): List<String> {
        if (!observedMessagesSemaphore.tryAcquire(expectedMessages, PERMIT_ACQUIRE_TIMEOUT, TimeUnit.SECONDS)) {
            Assert.fail("Timeout reached for getting the observed messages!")
        }
        return observedMessages
    }
}
