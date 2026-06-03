import { cp, mkdir, rm } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const viewerRoot = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const repositoryRoot = resolve(viewerRoot, "..", "..");
const source = resolve(repositoryRoot, "modules/core/build/dist/js/productionLibrary");
const destination = resolve(viewerRoot, ".generated/titlemanager-core");

await rm(destination, { recursive: true, force: true });
await mkdir(destination, { recursive: true });
await cp(source, destination, { recursive: true });
