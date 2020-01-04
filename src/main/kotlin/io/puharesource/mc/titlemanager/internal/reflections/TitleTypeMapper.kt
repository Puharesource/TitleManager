package io.puharesource.mc.titlemanager.internal.reflections

enum class TitleTypeMapper constructor(private val oldIndex: Int = -1, private val newIndex: Int) {
    TITLE       (0, 0),
    SUBTITLE    (1, 1),
    ACTIONBAR  (-1, 2),
    TIMES       (2, 3),
    CLEAR       (3, 4),
    RESET       (4, 5);

    val handle: Any
        get() {
            val provider = NMSManager.getClassProvider()

            // Protocol Hack
            if (NMSManager.versionIndex == 0) {
                return provider["Action"].handle.enumConstants[oldIndex]
            }

            val actions = provider["EnumTitleAction"].handle.enumConstants

            // Anything below 1.11
            if (NMSManager.versionIndex < 5) {
                return actions[oldIndex]
            }

            // 1.11 and above
            return actions[newIndex]
        }
}