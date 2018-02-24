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

package org.libreplicator.core.locator.api

import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface

private const val DEFAULT_MULTICAST_PORT = 24816
private const val DEFAULT_MULTICAST_PERIOD_IN_MILLISECONDS = 10_000L
private const val DEFAULT_BUFFER_SIZE_IN_BYTES = 1024

class NodeLocatorSettings private constructor(
    val multicastAddress: InetAddress,
    val multicastPort: Int,
    val multicastPeriodInMilliseconds: Long,
    val bufferSizeInBytes: Int
) {
    companion object {
        operator fun invoke(
                multicastAddress: String = "",
                multicastPort: Int = DEFAULT_MULTICAST_PORT,
                multicastPeriodInMilliseconds: Long = DEFAULT_MULTICAST_PERIOD_IN_MILLISECONDS,
                bufferSizeInBytes: Int = DEFAULT_BUFFER_SIZE_IN_BYTES
        ): NodeLocatorSettings {
            return NodeLocatorSettings(
                getMulticastAddress(
                    multicastAddress,
                    fallbackIpv4BroadcastAddress = "255.255.255.255",
                    fallbackIpv6MulticastAddress = "ff02::1"
                ),
                multicastPort,
                multicastPeriodInMilliseconds,
                bufferSizeInBytes
            )
        }

        private fun getMulticastAddress(
            multicastAddress: String,
            fallbackIpv4BroadcastAddress: String,
            fallbackIpv6MulticastAddress: String
        ): InetAddress {
            if (multicastAddress.isNotBlank()) return getAddress(multicastAddress)
            if (!isIpv6AddressAvailable()) return getAddress(fallbackIpv4BroadcastAddress)
            return getAddress(fallbackIpv6MulticastAddress)
        }

        private fun getAddress(multicastAddress: String) = InetAddress.getByName(multicastAddress)

        private fun isIpv6AddressAvailable(): Boolean {
            return NetworkInterface.getNetworkInterfaces().asSequence()
                .map { it.interfaceAddresses }
                .flatten()
                .map { it.address }
                .any { Inet6Address::class.isInstance(it) }
        }
    }
}
