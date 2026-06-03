import { mkdir, readFile, writeFile } from "node:fs/promises";
import { resolve } from "node:path";
import { dirname } from "node:path";
import { fileURLToPath } from "node:url";

const siteRoot = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const repositoryRoot = resolve(siteRoot, "..", "..");
const defaultsRoot = resolve(repositoryRoot, "modules/bukkit/defaults/src/main/resources");
const output = resolve(siteRoot, "docs/generated-defaults.mdx");
const files = [
  "DefaultConfigs/advanced.yml",
  "DefaultConfigs/player-list.yml",
  "DefaultConfigs/welcome-title.yml",
  "DefaultConfigs/welcome-actionbar.yml",
  "DefaultConfigs/scoreboard.yml",
  "DefaultConfigs/gradients.yml",
  "DefaultConfigs/announcer.yml",
  "DefaultConfigs/hooks.yml",
  "DefaultAnimations/left-to-right.txt",
  "DefaultAnimations/right-to-left.txt",
];

let markdown = `---\nid: generated-defaults\ntitle: Generated Default Files\n---\n\n# Generated Default Files\n\nThis page is generated from \`modules/bukkit/defaults/src/main/resources\` so the documentation stays aligned with the files shipped in the plugin jar.\n`;

for (const file of files) {
  const source = resolve(defaultsRoot, file);
  const contents = await readFile(source, "utf8");
  const language = file.endsWith(".yml") ? "yaml" : "text";
  markdown += `\n## \`${file}\`\n\n\`\`\`${language}\n${contents.trimEnd()}\n\`\`\`\n`;
}

await mkdir(dirname(output), { recursive: true });
await writeFile(output, markdown, "utf8");
