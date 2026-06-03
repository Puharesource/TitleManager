CREATE TABLE IF NOT EXISTS "playerinfo" (
    uuid                            TEXT    PRIMARY KEY,
    is_sidebar_enabled              INTEGER NOT NULL,
    is_player_list_enabled          INTEGER NOT NULL,
    is_welcome_title_enabled        INTEGER NOT NULL,
    is_welcome_actionbar_enabled    INTEGER NOT NULL
);