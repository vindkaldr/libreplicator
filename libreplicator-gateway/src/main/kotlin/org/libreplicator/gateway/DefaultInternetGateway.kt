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

import org.fourthline.cling.UpnpService
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
import org.libreplicator.gateway.api.InternetGateway
import org.libreplicator.gateway.api.model.AddPortMapping
import org.libreplicator.gateway.api.model.DeletePortMapping
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.util.concurrent.Semaphore
import javax.inject.Inject

class DefaultInternetGateway @Inject constructor() : InternetGateway {
    private companion object {
        private val logger = LoggerFactory.getLogger(DefaultInternetGateway::class.java)
    }

    suspend override fun addPortMapping(portMapping: AddPortMapping) {
        val upnpService = UpnpServiceImpl()
        val semaphore = Semaphore(0)

        upnpService.registry.addListener(object : DefaultRegistryListener() {
            override fun remoteDeviceAdded(registry: Registry, device: RemoteDevice) {
                if (isInternetGatewayDevice(device)) {
                    println("Remote device added! Registry: $registry, Device: $device")

                    callAction(
                            upnpService = upnpService,
                            device = device,
                            serviceTpe = "Layer3Forwarding",
                            serviceAction = "GetDefaultConnectionService",
                            afterInvocation = { invocation ->
                                val serviceTpe = invocation.getOutput("NewDefaultConnectionService").value.toString()
                                callAddPortMappingAction(upnpService, device, serviceTpe, "AddPortMapping", portMapping, semaphore)
                            },
                            afterInvocationFailure = { _, message ->
                                logger.error("Failed to add port mapping! $message")
                            })
                }
            }
        })

        upnpService.controlPoint.search(UDADeviceTypeHeader(UDADeviceType("InternetGatewayDevice")))
        semaphore.acquire()
        upnpService.shutdown()
    }

    suspend override fun deletePortMapping(portMapping: DeletePortMapping) {
        val upnpService = UpnpServiceImpl()
        val semaphore = Semaphore(0)

        upnpService.registry.addListener(object : DefaultRegistryListener() {
            override fun remoteDeviceAdded(registry: Registry, device: RemoteDevice) {
                if (isInternetGatewayDevice(device)) {
                    println("Remote device added! Registry: $registry, Device: $device")

                    callAction(
                            upnpService = upnpService,
                            device = device,
                            serviceTpe = "Layer3Forwarding",
                            serviceAction = "GetDefaultConnectionService",
                            afterInvocation = { invocation ->
                                val serviceTpe = invocation.getOutput("NewDefaultConnectionService").value.toString()
                                callDeletePortMappingAction(upnpService, device, serviceTpe, "DeletePortMapping", portMapping, semaphore)
                            },
                            afterInvocationFailure = { _, message ->
                                logger.error("Failed to delete port mapping! $message")
                            })
                }
            }
        })
        upnpService.controlPoint.search(UDADeviceTypeHeader(UDADeviceType("InternetGatewayDevice")))
        semaphore.acquire()
        upnpService.shutdown()
    }

    private fun isInternetGatewayDevice(device: RemoteDevice): Boolean =
            device.type == UDADeviceType("InternetGatewayDevice")

    private fun callAction(upnpService: UpnpService, device: RemoteDevice, serviceTpe: String, serviceAction: String,
            beforeInvocation: (ActionInvocation<out Service<*, *>>) -> Unit = {},
            afterInvocation: (ActionInvocation<out Service<*, *>>) -> Unit = {},
            afterInvocationFailure: (ActionInvocation<out Service<*, *>>, String) -> Unit = { _, _-> }) {

        val forwardingService = device.findService(UDAServiceType(serviceTpe))
        val defaultConnectionServiceAction = forwardingService.getAction(serviceAction)

        val actionInvocation = ActionInvocation(defaultConnectionServiceAction)
        beforeInvocation(actionInvocation)

        val defaultConnectionServiceCallback = object : ActionCallback(actionInvocation) {
            override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                if (invocation != null) {
                    afterInvocation(invocation)
                }
            }

            override fun failure(invocation: ActionInvocation<out Service<*, *>>, operation: UpnpResponse?, defaultMsg: String) {
                afterInvocationFailure(invocation, defaultMsg)
            }
        }

        upnpService.controlPoint.execute(defaultConnectionServiceCallback)
    }

    private fun callAddPortMappingAction(upnpService: UpnpService, device: RemoteDevice, serviceTpe: String, serviceAction: String,
            portMapping: AddPortMapping, semaphore: Semaphore) {
        callAction(
                upnpService = upnpService,
                device = device,
                serviceTpe = serviceTpe,
                serviceAction = serviceAction,
                beforeInvocation = { invocation ->
                    invocation.setInput("NewExternalPort", UnsignedIntegerTwoBytes(portMapping.externalPort.toLong()))
                    invocation.setInput("NewProtocol", portMapping.protocol.toString())
                    invocation.setInput("NewInternalPort", UnsignedIntegerTwoBytes(portMapping.internalPort.toLong()))
                    invocation.setInput("NewInternalClient", InetAddress.getLocalHost().hostAddress)
                    invocation.setInput("NewEnabled", true)
                    invocation.setInput("NewPortMappingDescription", portMapping.description)
                    invocation.setInput("NewLeaseDuration", UnsignedIntegerFourBytes(0))
                },
                afterInvocation = {
                    semaphore.release()
                },
                afterInvocationFailure = { _, message ->
                    logger.error("Failed to delete port mapping! $message")
                })
    }

    private fun callDeletePortMappingAction(upnpService: UpnpService, device: RemoteDevice, serviceTpe: String, serviceAction: String,
            portMapping: DeletePortMapping, semaphore: Semaphore) {
        callAction(
                upnpService = upnpService,
                device = device,
                serviceTpe = serviceTpe,
                serviceAction = serviceAction,
                beforeInvocation = { invocation ->
                    invocation.setInput("NewExternalPort", UnsignedIntegerTwoBytes(portMapping.externalPort.toLong()))
                    invocation.setInput("NewProtocol", portMapping.protocol.toString())
                },
                afterInvocation = {
                    semaphore.release()
                },
                afterInvocationFailure = { _, message ->
                    logger.error("Failed to delete port mapping! $message")
                })
    }
}
