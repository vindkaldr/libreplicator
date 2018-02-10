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

package org.libreplicator.gateway

import org.bitlet.weupnp.GatewayDevice
import org.bitlet.weupnp.GatewayDiscover
import org.bitlet.weupnp.PortMappingEntry
import org.libreplicator.gateway.api.InternetGateway
import org.libreplicator.gateway.api.InternetGatewayException
import org.libreplicator.gateway.api.model.AddPortMapping
import org.libreplicator.gateway.api.model.DeletePortMapping
import org.libreplicator.gateway.api.model.PortMapping
import org.libreplicator.log.api.trace
import java.net.InetAddress
import javax.inject.Inject

class DefaultInternetGateway @Inject constructor() : InternetGateway {
    override fun addPortMapping(portMapping: AddPortMapping) {
        trace("Adding port mapping..")
        val device = discoverGatewayDevice() ?: throw InternetGatewayException("Failed to discover gateway device!")

        val existingPortMappingEntry = PortMappingEntry()

        if (isSpecificPortMappingExists(device, portMapping, existingPortMappingEntry)) {
            checkPortMappingNotSetByOtherDevice(existingPortMappingEntry)
            return
        }

        addPortMapping(device, portMapping)
        trace("Port mapping added")
    }

    override fun deletePortMapping(portMapping: DeletePortMapping) {
        trace("Deleting port mapping..")
        val device = discoverGatewayDevice() ?: throw InternetGatewayException("Failed to discover gateway device!")

        val existingPortMappingEntry = PortMappingEntry()

        if (!isSpecificPortMappingExists(device, portMapping, existingPortMappingEntry)) {
            trace("Port is not mapped!")
            return
        }

        checkPortMappingNotSetByOtherDevice(existingPortMappingEntry)
        deletePortMapping(device, portMapping)
        trace("Port mapping deleted")
    }

    private fun discoverGatewayDevice(): GatewayDevice? {
        val gatewayDiscover = GatewayDiscover()
        gatewayDiscover.discover()
        return gatewayDiscover.validGateway
    }

    private fun isSpecificPortMappingExists(device: GatewayDevice, portMapping: PortMapping, portMappingEntry: PortMappingEntry)
            = device.getSpecificPortMappingEntry(portMapping.externalPort, portMapping.protocol.toString(), portMappingEntry)

    private fun checkPortMappingNotSetByOtherDevice(existingPortMappingEntry: PortMappingEntry) {
        trace("Port mapping exists!")
        if (isPortMappingSetByOtherDevice(existingPortMappingEntry)) {
            throw InternetGatewayException("Port mapping exists by other device!")
        }
    }

    private fun isPortMappingSetByOtherDevice(portMappingEntry: PortMappingEntry)
            = portMappingEntry.internalClient != getOwnInternalIpAddress()

    private fun getOwnInternalIpAddress() = InetAddress.getLocalHost().hostAddress

    private fun addPortMapping(device: GatewayDevice, portMapping: AddPortMapping) {
        val success = device.addPortMapping(
                portMapping.externalPort, portMapping.internalPort, getOwnInternalIpAddress(),
                portMapping.protocol.toString(), portMapping.description)

        if (!success) {
            throw InternetGatewayException("Failed to add port mapping!")
        }
    }

    private fun deletePortMapping(device: GatewayDevice, portMapping: DeletePortMapping) {
        device.deletePortMapping(portMapping.externalPort, portMapping.protocol.toString())
    }
}
