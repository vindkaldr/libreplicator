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

package org.libreplicator.network.cipher

import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Assert.assertThat
import org.junit.Test

class PasswordBasedMessageCipherTest {
    private companion object {
        private val PASSWORD = "pasword".toByteArray()
        private val CORRUPTED_PASSWORD = "corruptedPassword".toByteArray()

        private val MESSAGE = "message"
        private val ENCRYPTED_MESSAGE = "4ae2942fcc44b41761c9ea86c4e63764"
        private val CORRUPTED_MESSAGE = "corruptedMessage"
    }

    private val messageCipher = PasswordBasedMessageCipher()

    @Test
    fun encrypt_encryptsMessage() {
        assertThat(messageCipher.encrypt(PASSWORD, MESSAGE), not(equalTo(MESSAGE)))
    }

    @Test
    fun decrypt_decryptsMessage() {
        assertThat(messageCipher.decrypt(PASSWORD, ENCRYPTED_MESSAGE), not(equalTo(ENCRYPTED_MESSAGE)))
    }

    @Test
    fun cipher_encryptsThenDecryptsMessage_withSamePassword() {
        val encryptedMessage = messageCipher.encrypt(PASSWORD, MESSAGE)
        assertThat(messageCipher.decrypt(PASSWORD, encryptedMessage), equalTo(MESSAGE))
    }

    @Test(expected = CipherException::class)
    fun decrypt_throwsException_forCorruptedPassword() {
        messageCipher.decrypt(CORRUPTED_PASSWORD, messageCipher.encrypt(PASSWORD, MESSAGE))
    }

    @Test(expected = CipherException::class)
    fun decrypt_throwsException_forCorruptedMessage() {
        messageCipher.decrypt(PASSWORD, CORRUPTED_MESSAGE)
    }
}
