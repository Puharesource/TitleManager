import { cp, rm } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const siteRoot = resolve(__dirname, "..");
const repositoryRoot = resolve(siteRoot, "..", "..");
const source = resolve(repositoryRoot, "modules/core/build/dist/js/productionLibrary");
const target = resolve(siteRoot, ".generated", "titlemanager-core");

await rm(target, { recursive: true, force: true });
await cp(source, target, { recursive: true });
