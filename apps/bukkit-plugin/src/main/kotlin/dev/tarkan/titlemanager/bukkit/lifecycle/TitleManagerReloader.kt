package dev.tarkan.titlemanager.bukkit.lifecycle

fun interface TitleManagerReloader {
    fun reload()
}

class TransactionalTitleManagerReloader(
    private val isRuntimeStarted: () -> Boolean,
    private val validateConfiguration: () -> Unit,
    private val disableRuntime: () -> Unit,
    private val enableRuntime: () -> Unit
) : TitleManagerReloader {
    override fun reload() {
        if (isRuntimeStarted()) {
            validateConfiguration()
        }

        disableRuntime()
        enableRuntime()
    }
}
