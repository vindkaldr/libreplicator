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

package org.libreplicator.crypto

import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Assert.assertThat
import org.junit.Test
import org.libreplicator.crypto.api.CipherException
import org.libreplicator.crypto.api.MessageCipher

class DefaultMessageCipherTest {
    private companion object {
        private val SHARED_SECRET = "sharedSecret"
        private val CORRUPTED_SHARED_SECRET = "corruptedSharedSecret"

        private val MESSAGE = "message"
        private val ENCRYPTED_MESSAGE = "a34202b818283972eea9cb5cacdec659"
        private val CORRUPTED_MESSAGE = "corruptedMessage"
    }

    @Test
    fun encrypt_encryptsMessage() {
        val messageCipher: MessageCipher = DefaultMessageCipher(SHARED_SECRET)
        assertThat(messageCipher.encrypt(MESSAGE), not(equalTo(MESSAGE)))
    }

    @Test
    fun decrypt_decryptsMessage() {
        val messageCipher: MessageCipher = DefaultMessageCipher(SHARED_SECRET)
        assertThat(messageCipher.decrypt(ENCRYPTED_MESSAGE), not(equalTo(ENCRYPTED_MESSAGE)))
    }

    @Test
    fun cipher_encryptsThenDecryptsMessage_withSameSharedSecret() {
        val messageCipher: MessageCipher = DefaultMessageCipher(SHARED_SECRET)

        val encryptedMessage = messageCipher.encrypt(MESSAGE)
        val decryptedMessage = messageCipher.decrypt(encryptedMessage)

        assertThat(decryptedMessage, equalTo(MESSAGE))
    }

    @Test(expected = CipherException::class)
    fun decrypt_throwsException_forCorruptedSharedSecret() {
        val messageCipher: MessageCipher = DefaultMessageCipher(CORRUPTED_SHARED_SECRET)
        messageCipher.decrypt(ENCRYPTED_MESSAGE)
    }

    @Test(expected = CipherException::class)
    fun decrypt_throwsException_forCorruptedMessage() {
        val messageCipher: MessageCipher = DefaultMessageCipher(SHARED_SECRET)
        messageCipher.decrypt(CORRUPTED_MESSAGE)
    }
}
