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

import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.libreplicator.api.AlreadySubscribedException
import org.libreplicator.api.NotSubscribedException
import org.libreplicator.api.Subscription

class InsecureChannelIntegrationTest {
    private companion object {
        private val URL = "localhost"
        private val PORT = 3600
        private val REMOTE_URL = URL
        private val REMOTE_PORT = 3601

        private val MESSAGE = "message"
        private val OTHER_MESSAGE = "otherMessage"
    }

    private lateinit var insecureChannel: InsecureChannel
    private lateinit var messageObserver: MessageObserverDummy
    private lateinit var subscription: Subscription

    private lateinit var remoteInsecureChannel: InsecureChannel
    private lateinit var remoteMessageObserver: MessageObserverMock
    private lateinit var remoteSubscription: Subscription

    @Before
    fun setUp() {
        insecureChannel = InsecureChannel(URL, PORT)
        messageObserver = MessageObserverDummy()

        remoteInsecureChannel = InsecureChannel(REMOTE_URL, REMOTE_PORT)
    }

    @After
    fun tearDown() {
        if (insecureChannel.hasSubscription()) {
            subscription.unsubscribe()
        }
        if (remoteInsecureChannel.hasSubscription()) {
            remoteSubscription.unsubscribe()
        }
    }

    @Test
    fun canSubscribeToChannel() {
        subscription = insecureChannel.subscribe(messageObserver)
    }

    @Test(expected = AlreadySubscribedException::class)
    fun canNotSubscribeToChannel_ifAlreadySubscribed() {
        subscription = insecureChannel.subscribe(messageObserver)

        insecureChannel.subscribe(messageObserver)
    }

    @Test
    fun canUnsubscribeFromChannel() {
        subscription = insecureChannel.subscribe(messageObserver)

        subscription.unsubscribe()
    }

    @Test(expected = NotSubscribedException::class)
    fun canNotUnsubscribeFromChannel_ifAlreadyUnsubscribed() {
        subscription = insecureChannel.subscribe(messageObserver)
        subscription.unsubscribe()

        subscription.unsubscribe()
    }

    @Test
    fun channel_receivesSentMessage() {
        subscription = insecureChannel.subscribe(messageObserver)
        remoteMessageObserver = MessageObserverMock.createWithExpectedMessages(1)
        remoteSubscription = remoteInsecureChannel.subscribe(remoteMessageObserver)

        insecureChannel.send(REMOTE_URL, REMOTE_PORT, MESSAGE)

        assertThat(remoteMessageObserver.getObservedMessages(), equalTo(listOf(MESSAGE)))
    }

    @Test
    fun channel_receivesSentMessages() {
        subscription = insecureChannel.subscribe(messageObserver)
        remoteMessageObserver = MessageObserverMock.createWithExpectedMessages(2)
        remoteSubscription = remoteInsecureChannel.subscribe(remoteMessageObserver)

        insecureChannel.send(REMOTE_URL, REMOTE_PORT, MESSAGE)
        insecureChannel.send(REMOTE_URL, REMOTE_PORT, OTHER_MESSAGE)

        assertThat(remoteMessageObserver.getObservedMessages(), equalTo(listOf(MESSAGE, OTHER_MESSAGE)))
    }

    @Test
    fun unsubscribingFromChannel_isABlockingOperation() {
        subscription = insecureChannel.subscribe(messageObserver)

        subscription.unsubscribe()

        assertThat(insecureChannel.hasSubscription(), equalTo(false))
    }
}
