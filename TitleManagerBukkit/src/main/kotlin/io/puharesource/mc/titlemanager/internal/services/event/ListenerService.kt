package io.puharesource.mc.titlemanager.internal.services.event

interface ListenerService {
    fun registerListeners()
    fun unregisterListeners()
}
