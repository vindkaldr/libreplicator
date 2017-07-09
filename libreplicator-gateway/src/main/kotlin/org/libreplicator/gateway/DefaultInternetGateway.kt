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

package org.libreplicator.gateway

import org.fourthline.cling.UpnpServiceImpl
import org.fourthline.cling.controlpoint.ActionCallback
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.message.header.UDADeviceTypeHeader
import org.fourthline.cling.model.meta.RemoteDevice
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UDAServiceType
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes
import org.fourthline.cling.registry.DefaultRegistryListener
import org.fourthline.cling.registry.Registry
import org.libreplicator.gateway.api.model.AddPortMapping
import org.libreplicator.gateway.api.model.DeletePortMapping
import org.libreplicator.gateway.api.InternetGateway
import java.net.InetAddress

class DefaultInternetGateway : InternetGateway {
    override fun addPortMapping(portMapping: AddPortMapping) {
        val upnpService = UpnpServiceImpl()

        upnpService.registry.addListener(object : DefaultRegistryListener() {
            override fun remoteDeviceAdded(registry: Registry, device: RemoteDevice) {
                if (isInternetGatewayDevice(device)) {
                    println("Remote device added! Registry: $registry, Device: $device")

                    val forwardingService = device.findService(UDAServiceType("Layer3Forwarding"))
                    val defaultConnectionServiceAction = forwardingService.getAction("GetDefaultConnectionService")

                    val defaultConnectionServiceCallback = object : ActionCallback(ActionInvocation(defaultConnectionServiceAction)) {
                        override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                            if (invocation != null) {
                                val defaultConnectionService = invocation.getOutput("NewDefaultConnectionService").value
                                println(defaultConnectionService)

                                if (defaultConnectionService != null && defaultConnectionService is String) {
                                    val connectionService = device.findService(UDAServiceType(defaultConnectionService))

                                    val portMappingAction = connectionService.getAction("AddPortMapping")
                                    val portMappingInvocation = ActionInvocation(portMappingAction)

                                    portMappingInvocation.setInput("NewExternalPort", UnsignedIntegerTwoBytes(portMapping.externalPort.toLong()))
                                    portMappingInvocation.setInput("NewProtocol", portMapping.protocol.toString())
                                    portMappingInvocation.setInput("NewInternalPort", UnsignedIntegerTwoBytes(portMapping.internalPort.toLong()))
                                    portMappingInvocation.setInput("NewInternalClient", InetAddress.getLocalHost().hostAddress)
                                    portMappingInvocation.setInput("NewEnabled", true)
                                    portMappingInvocation.setInput("NewPortMappingDescription", portMapping.description)
                                    portMappingInvocation.setInput("NewLeaseDuration", UnsignedIntegerFourBytes(0))

                                    val addPortMappingCallback = object : ActionCallback(portMappingInvocation) {
                                        override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                                            println("Success")
                                        }

                                        override fun failure(invocation: ActionInvocation<out Service<*, *>>?, operation: UpnpResponse?, defaultMsg: String?) {
                                            println(defaultMsg!!)
                                        }
                                    }
                                    upnpService.controlPoint.execute(addPortMappingCallback)
                                }
                            }
                        }

                        override fun failure(invocation: ActionInvocation<out Service<*, *>>?, operation: UpnpResponse?, defaultMsg: String?) {
                            error(defaultMsg!!)
                        }
                    }
                    upnpService.controlPoint.execute(defaultConnectionServiceCallback)
                }
            }

            private fun isInternetGatewayDevice(device: RemoteDevice): Boolean =
                    device.type == UDADeviceType("InternetGatewayDevice")
        })

        upnpService.controlPoint.search(UDADeviceTypeHeader(UDADeviceType("InternetGatewayDevice")))
        upnpService.shutdown()
    }

    override fun deletePortMapping(portMapping: DeletePortMapping) {
        val upnpService = UpnpServiceImpl()

        upnpService.registry.addListener(object : DefaultRegistryListener() {
            override fun remoteDeviceAdded(registry: Registry, device: RemoteDevice) {
                if (isInternetGatewayDevice(device)) {
                    println("Remote device added! Registry: $registry, Device: $device")

                    val forwardingService = device.findService(UDAServiceType("Layer3Forwarding"))
                    val defaultConnectionServiceAction = forwardingService.getAction("GetDefaultConnectionService")

                    val defaultConnectionServiceCallback = object : ActionCallback(ActionInvocation(defaultConnectionServiceAction)) {
                        override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                            if (invocation != null) {
                                val defaultConnectionService = invocation.getOutput("NewDefaultConnectionService").value
                                println(defaultConnectionService)

                                if (defaultConnectionService != null && defaultConnectionService is String) {
                                    val connectionService = device.findService(UDAServiceType(defaultConnectionService))

                                    val portMappingAction = connectionService.getAction("DeletePortMapping")
                                    val portMappingInvocation = ActionInvocation(portMappingAction)

                                    portMappingInvocation.setInput("NewExternalPort", UnsignedIntegerTwoBytes(portMapping.externalPort.toLong()))
                                    portMappingInvocation.setInput("NewProtocol", portMapping.protocol.toString())

                                    val addPortMappingCallback = object : ActionCallback(portMappingInvocation) {
                                        override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                                            println("Success")
                                        }

                                        override fun failure(invocation: ActionInvocation<out Service<*, *>>?, operation: UpnpResponse?, defaultMsg: String?) {
                                            println(defaultMsg!!)
                                        }
                                    }
                                    upnpService.controlPoint.execute(addPortMappingCallback)
                                }
                            }
                        }

                        override fun failure(invocation: ActionInvocation<out Service<*, *>>?, operation: UpnpResponse?, defaultMsg: String?) {
                            error(defaultMsg!!)
                        }

                    }

                    upnpService.controlPoint.execute(defaultConnectionServiceCallback)
                }
            }

            private fun isInternetGatewayDevice(device: RemoteDevice): Boolean =
                    device.type == UDADeviceType("InternetGatewayDevice")
        })

        upnpService.controlPoint.search(UDADeviceTypeHeader(UDADeviceType("InternetGatewayDevice")))
        upnpService.shutdown()
    }
}
