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

package org.libreplicator.network.crypto

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECParameterSpec
import org.bouncycastle.util.encoders.Hex
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.Security
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement

class SharedSecretGenerator {
    private companion object {
        init {
            Security.addProvider(BouncyCastleProvider());
        }
    }

    fun generateKeyPair(): GeneratedKeyPair {
        val parameterSpec: ECParameterSpec = ECNamedCurveTable.getParameterSpec("B-571");

        val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance("ECDH", "BC");
        keyPairGenerator.initialize(parameterSpec, SecureRandom());

        val keyPair: KeyPair = keyPairGenerator.generateKeyPair();
        return GeneratedKeyPair(keyPair)
    }

    fun generateSharedSecret(generatedKeyPair: GeneratedKeyPair, otherPublicKey: String): ByteArray {
        val otherPublic = KeyFactory.getInstance("ECDH", "BC")
                .generatePublic(X509EncodedKeySpec(Hex.decode(otherPublicKey.toByteArray())))

        val keyAgreement: KeyAgreement = KeyAgreement.getInstance("ECDH", "BC");
        keyAgreement.init(generatedKeyPair.privateKey);
        keyAgreement.doPhase(otherPublic, true);

        val hash = MessageDigest.getInstance("SHA512", "BC")
        return hash.digest(keyAgreement.generateSecret())
    }

    class GeneratedKeyPair(private val keyPair: KeyPair) {
        val privateKey: PrivateKey
            get() = keyPair.private

        val publicKey: String
            get() = String(Hex.encode(keyPair.public.encoded))
    }
}
