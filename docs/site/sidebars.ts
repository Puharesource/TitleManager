import type { SidebarsConfig } from "@docusaurus/plugin-content-docs";

const sidebars: SidebarsConfig = {
  docsSidebar: [
    "introduction",
    "migration-2-to-3",
    {
      type: "category",
      label: "Timings",
      link: { type: "doc", id: "timings" },
      items: ["timings-syntax"],
    },
    {
      type: "category",
      label: "Commands",
      link: { type: "doc", id: "commands" },
      items: [
        "broadcast-title-command",
        "message-title-command",
        "broadcast-actionbar-message-command",
        "message-actionbar-command",
        "scoreboard-toggle-command",
        "list-animations-command",
        "list-scripts-command",
        "reload-command",
        "version-command",
      ],
    },
    {
      type: "category",
      label: "Server Administrators",
      link: { type: "doc", id: "server-administrators" },
      items: [
        "setup",
        "configuration",
        "generated-defaults",
        "placeholders",
        "permissions",
        "animations",
        "built-in-animation-examples",
        "animation-playground",
        "scripts",
        "troubleshooting",
      ],
    },
    {
      type: "category",
      label: "Developers",
      link: { type: "doc", id: "developers" },
      items: [
        "getting-started",
        "title-messages",
        "actionbar-messages",
        "player-list",
        "scoreboard",
        "web-viewer",
        "api-reference",
        "runtime-support",
      ],
    },
  ],
};

export default sidebars;
