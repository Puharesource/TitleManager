package io.puharesource.mc.titlemanager.internal.extensions

import org.bukkit.scheduler.BukkitTask

fun BukkitTask.addTo(collection: MutableCollection<BukkitTask>) = collection.add(this)