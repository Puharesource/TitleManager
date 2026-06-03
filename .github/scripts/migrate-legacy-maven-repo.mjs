#!/usr/bin/env node
import { mkdir, readFile, writeFile } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import path from 'node:path';

const legacyBaseUrl = normalizeBaseUrl(process.env.LEGACY_MAVEN_REPOSITORY_URL || 'https://repo.tarkan.dev/');
const outputRoot = process.env.MAVEN_REPOSITORY_ROOT || 'build/maven-repository';
const legacyMetadataPath = 'io/puharesource/mc/TitleManager/maven-metadata.xml';
const artifactId = 'TitleManager';
const fetchAttempts = Number(process.env.LEGACY_MAVEN_FETCH_ATTEMPTS || 4);
const fetchTimeoutMilliseconds = Number(process.env.LEGACY_MAVEN_FETCH_TIMEOUT_MILLISECONDS || 60_000);

function sleep(milliseconds) {
  return new Promise((resolve) => setTimeout(resolve, milliseconds));
}


function normalizeBaseUrl(value) {
  return value.endsWith('/') ? value : `${value}/`;
}

async function fetchBytes(relativePath, required = false) {
  const url = new URL(relativePath, legacyBaseUrl);
  let lastError;
  for (let attempt = 1; attempt <= fetchAttempts; attempt += 1) {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), fetchTimeoutMilliseconds);
    try {
      const response = await fetch(url, {
        headers: { 'user-agent': 'TitleManager-release-migration' },
        signal: controller.signal,
      });
      if (response.status === 404) {
        if (required) throw new Error(`Required legacy Maven artifact is unavailable: ${relativePath}`);
        return null;
      }
      if (!response.ok) throw new Error(`Failed to fetch ${url}: HTTP ${response.status}`);

      const contentType = response.headers.get('content-type') || '';
      const bytes = Buffer.from(await response.arrayBuffer());
      if (contentType.startsWith('text/html') && bytes.subarray(0, 4096).includes(Buffer.from('Cloudflare Workers'))) {
        if (required) throw new Error(`Required legacy Maven artifact returned Cloudflare Workers HTML: ${relativePath}`);
        return null;
      }
      return bytes;
    } catch (error) {
      lastError = error;
      if (attempt === fetchAttempts) break;
      await sleep(250 * attempt);
    } finally {
      clearTimeout(timeout);
    }
  }
  throw lastError;

}

async function writeIfChanged(relativePath, bytes) {
  const destination = path.join(outputRoot, relativePath);
  await mkdir(path.dirname(destination), { recursive: true });
  if (existsSync(destination)) {
    const existing = await readFile(destination);
    if (Buffer.compare(existing, bytes) === 0) return false;
  }
  await writeFile(destination, bytes);
  return true;
}

async function download(relativePath, required = false) {
  const bytes = await fetchBytes(relativePath, required);
  if (bytes === null) return false;
  return writeIfChanged(relativePath, bytes);
}

function parseVersions(metadata) {
  const text = metadata.toString('utf8');
  const versions = [...text.matchAll(/<version>([^<]+)<\/version>/g)].map((match) => match[1]);
  if (versions.length === 0) throw new Error('Legacy Maven metadata did not contain any versions.');
  return versions;
}

function artifactPaths(version) {
  const base = `io/puharesource/mc/${artifactId}/${version}/${artifactId}-${version}`;
  const paths = [];
  for (const suffix of ['.pom', '.jar', '-sources.jar', '-javadoc.jar']) {
    const artifact = `${base}${suffix}`;
    paths.push(artifact, `${artifact}.sha1`, `${artifact}.md5`);
  }
  return paths;
}

const metadata = await fetchBytes(legacyMetadataPath);
if (metadata === null) {
  console.log(`No legacy Maven metadata found at ${new URL(legacyMetadataPath, legacyBaseUrl)}; skipping legacy migration.`);
  process.exit(0);
}

await writeIfChanged(legacyMetadataPath, metadata);
for (const checksumSuffix of ['.sha1', '.md5']) {
  await download(`${legacyMetadataPath}${checksumSuffix}`);
}

const versions = parseVersions(metadata);
let present = 0;
let changed = 0;
for (const version of versions) {
  for (const artifactPath of artifactPaths(version)) {
    if (await download(artifactPath)) changed += 1;
    if (existsSync(path.join(outputRoot, artifactPath))) present += 1;
  }
}

console.log(`Migrated legacy Maven metadata for ${versions.length} versions from ${legacyBaseUrl}`);
console.log(`Legacy Maven files present: ${present}; changed: ${changed}`);
