package dev.tarkan.titlemanager.test.utils

object TestFixtures {
    data class OldTitleManagerConfigTextEntry(val path: String, val text: String)

    fun animationFile(name: String): String = when (name) {
        "animation_file_all.txt" -> """
            line 1
            line 2
            [1]line 3
            line 4
            line 5
            [1;2;3]line 6
            [4;5;6]line 7
            line 8
        """.trimIndent()

        "animation_file_simple.txt" -> """
            [500]line 1
            [1000]line 2
            [750]line 3
            [200]line 4
        """.trimIndent()

        "animation_file_simple_with_variables.txt" -> """
            [500]line 1 %{var1}
            [1000]line 2 %{var2:with data}
            [750]line 3 %{var3} %{var1}
            [200]line 4 %{var1:with data} %{var2}
        """.trimIndent()

        "animation_file_simple_with_animations.txt" -> """
            [500]line 1 ${'$'}{countdown:5}
            [500]line 2 ${'$'}{countdown:3}
        """.trimIndent()

        "animation_file_combined_with_animations.txt" -> """
            line 1: ${'$'}{countdown:3}
            line 2: ${'$'}{countdown:3} ${'$'}{countdown:3}
        """.trimIndent()

        "old-titlemanager-left-to-right.txt" -> """
            [0;5;0]&7&b-&7---------
            [0;2;0]&7-&b-&7--------
            [0;2;0]&7--&b-&7-------
            [0;2;0]&7---&b-&7------
            [0;2;0]&7----&b-&7-----
            [0;2;0]&7-----&b-&7----
            [0;2;0]&7------&b-&7---
            [0;2;0]&7-------&b-&7--
            [0;2;0]&7--------&b-&7-
            [0;5;0]&7---------&b-&7
            [0;2;0]&7--------&b-&7-
            [0;2;0]&7-------&b-&7--
            [0;2;0]&7------&b-&7---
            [0;2;0]&7-----&b-&7----
            [0;2;0]&7----&b-&7-----
            [0;2;0]&7---&b-&7------
            [0;2;0]&7--&b-&7-------
            [0;2;0]&7-&b-&7--------
        """.trimIndent()

        "old-titlemanager-right-to-left.txt" -> """
            [0;5;0]&7---------&b-&7
            [0;2;0]&7--------&b-&7-
            [0;2;0]&7-------&b-&7--
            [0;2;0]&7------&b-&7---
            [0;2;0]&7-----&b-&7----
            [0;2;0]&7----&b-&7-----
            [0;2;0]&7---&b-&7------
            [0;2;0]&7--&b-&7-------
            [0;2;0]&7-&b-&7--------
            [0;5;0]&7&b-&7---------
            [0;2;0]&7-&b-&7--------
            [0;2;0]&7--&b-&7-------
            [0;2;0]&7---&b-&7------
            [0;2;0]&7----&b-&7-----
            [0;2;0]&7-----&b-&7----
            [0;2;0]&7------&b-&7---
            [0;2;0]&7-------&b-&7--
            [0;2;0]&7--------&b-&7-
        """.trimIndent()

        else -> error("Unknown test fixture: $name")
    }

    fun oldTitleManagerDefaultConfigTextEntries(): List<OldTitleManagerConfigTextEntry> = listOf(
        OldTitleManagerConfigTextEntry("player-list.header[0]", ""),
        OldTitleManagerConfigTextEntry(
            "player-list.header[1]",
            "${'$'}{shine:[0;2;0][0;25;0][0;25;0][&3;&b]My Server}"
        ),
        OldTitleManagerConfigTextEntry("player-list.header[2]", ""),
        OldTitleManagerConfigTextEntry("player-list.footer[0]", ""),
        OldTitleManagerConfigTextEntry("player-list.footer[1]", "&7World time: &b%{12h-world-time}"),
        OldTitleManagerConfigTextEntry("player-list.footer[2]", "&7Server time: &b%{server-time}"),
        OldTitleManagerConfigTextEntry("player-list.footer[3]", ""),
        OldTitleManagerConfigTextEntry(
            "player-list.footer[4]",
            "${'$'}{right-to-left} &b%{online}&7/&b%{max} &7Online Players ${'$'}{left-to-right}"
        ),
        OldTitleManagerConfigTextEntry("welcome-title.title", "Welcome to My Server"),
        OldTitleManagerConfigTextEntry("welcome-title.subtitle", "Hope you enjoy your stay"),
        OldTitleManagerConfigTextEntry("welcome-title.first-join.title", "Welcome to My Server"),
        OldTitleManagerConfigTextEntry("welcome-title.first-join.subtitle", "This is your first time!"),
        OldTitleManagerConfigTextEntry("welcome-actionbar.title", "Welcome to My Server"),
        OldTitleManagerConfigTextEntry(
            "welcome-actionbar.first-join",
            "Welcome to My Server, this is your first time!"
        ),
        OldTitleManagerConfigTextEntry(
            "scoreboard.title",
            "${'$'}{shine:[0;2;0][0;25;0][0;25;0][&3&l;&b&l]My Server}"
        ),
        OldTitleManagerConfigTextEntry("scoreboard.lines[0]", "&b&m----------------------------------"),
        OldTitleManagerConfigTextEntry("scoreboard.lines[1]", "&b> &3&lPlayer Name:"),
        OldTitleManagerConfigTextEntry("scoreboard.lines[2]", "&b%{name}"),
        OldTitleManagerConfigTextEntry("scoreboard.lines[3]", "&r"),
        OldTitleManagerConfigTextEntry("scoreboard.lines[4]", "&b> &3&lPing:"),
        OldTitleManagerConfigTextEntry("scoreboard.lines[5]", "&b%{ping} MS"),
        OldTitleManagerConfigTextEntry("scoreboard.lines[6]", "&r&r"),
        OldTitleManagerConfigTextEntry("scoreboard.lines[7]", "&b> &3&lServer Time:"),
        OldTitleManagerConfigTextEntry("scoreboard.lines[8]", "&b%{server-time}"),
        OldTitleManagerConfigTextEntry("scoreboard.lines[9]", "&b&m----------------------------------&r"),
        OldTitleManagerConfigTextEntry(
            "announcer.announcements.my-announcement.titles[0]",
            "&aThis is the 1st title announcement\\n&aThis is the subtitle"
        ),
        OldTitleManagerConfigTextEntry(
            "announcer.announcements.my-announcement.titles[1]",
            "&bThis is the 2nd title announcement\\n&bThis is the subtitle"
        ),
        OldTitleManagerConfigTextEntry(
            "announcer.announcements.my-announcement.actionbar[0]",
            "&aThis is the 1st actionbar announcement"
        ),
        OldTitleManagerConfigTextEntry(
            "announcer.announcements.my-announcement.actionbar[1]",
            "&bThis is the 2nd actionbar announcement"
        )
    )
}
