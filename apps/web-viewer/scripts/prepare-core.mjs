import { spawn } from "node:child_process";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const viewerRoot = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const repositoryRoot = resolve(viewerRoot, "..");

const gradleEnvironment = { ...process.env };
delete gradleEnvironment.JAVA_HOME;

await runGradle([
  ":modules:core:jsNodeProductionLibraryDistribution",
  "--no-daemon",
  "--console=plain",
  "--quiet"
]);

await import("./copy-core.mjs");

function runGradle(args) {
  const command = process.platform === "win32" ? "cmd" : "sh";
  const commandArgs =
    process.platform === "win32"
      ? ["/c", "gradlew.bat", ...args]
      : ["./gradlew", ...args];

  return new Promise((resolvePromise, reject) => {
    const child = spawn(command, commandArgs, {
      cwd: repositoryRoot,
      env: gradleEnvironment,
      stdio: "inherit"
    });

    child.on("error", reject);
    child.on("exit", (code) => {
      if (code === 0) {
        resolvePromise();
      } else {
        reject(new Error(`Gradle exited with code ${code}.`));
      }
    });
  });
}
