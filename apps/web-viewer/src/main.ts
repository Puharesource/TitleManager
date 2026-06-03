import { TitleManagerCoreApi } from "@titlemanager/core";
import "./styles.css";

const api = TitleManagerCoreApi.getInstance();

const defaultAnimation = "${shine:[20;60;20][&3;&b]TitleManager Next}\n[20]&7Shared core preview";
const defaultTimingScale = 50;
const defaultLimits = {
  maxInputCharacters: 10_000,
  maxLines: 500,
  maxFrames: 2_000,
  maxOutputCharacters: 100_000
};

type TimelineFrame = {
  startMilliseconds: number;
  endMilliseconds?: number | null;
  text: string;
};

type TimelineJsonExport = {
  text?: unknown;
  timingScale?: unknown;
  limits?: Partial<Record<keyof typeof defaultLimits, unknown>>;
};

function requireElement<T extends Element>(selector: string, parent: ParentNode = document): T {
  const element = parent.querySelector<T>(selector);
  if (element === null) {
    throw new Error(`Missing required element: ${selector}`);
  }
  return element;
}

const app = requireElement<HTMLElement>("#app");

app.innerHTML = `
  <section class="viewer">
    <header>
      <p class="eyebrow">TitleManager Web Viewer</p>
      <h1>Preview animations with the shared core engine</h1>
      <p class="intro">This page calls the generated @titlemanager/core TypeScript API, so web previews use the same parser and scheduler-free timeline model as the Minecraft plugin.</p>
    </header>
    <label class="editor-label" for="animation-input">Animation text</label>
    <textarea id="animation-input" spellcheck="false"></textarea>
    <label class="timing-scale-label" for="timing-scale">
      Timing scale
      <span>Milliseconds per timing unit. Use 50 for old Minecraft tick-based config syntax.</span>
      <input id="timing-scale" type="number" min="1" step="1" />
    </label>
    <fieldset class="limits">
      <legend>Preview safety limits</legend>
      <label>
        Input characters
        <input id="max-input-characters" type="number" min="1" step="1" />
      </label>
      <label>
        Lines
        <input id="max-lines" type="number" min="1" step="1" />
      </label>
      <label>
        Frames
        <input id="max-frames" type="number" min="1" step="1" />
      </label>
      <label>
        Output characters
        <input id="max-output-characters" type="number" min="1" step="1" />
      </label>
    </fieldset>
    <section class="summary" aria-live="polite"></section>
    <section class="preview-card" aria-label="Live animation preview">
      <div class="preview-toolbar">
        <label>
          <input id="autoplay-preview" type="checkbox" checked />
          Play preview
        </label>
        <div class="preview-actions">
          <button id="restart-preview" type="button">Restart</button>
          <button id="copy-share-url" type="button">Copy share link</button>
          <button id="import-timeline-json" type="button">Import timeline JSON</button>
          <button id="copy-timeline-json" type="button" disabled>Copy timeline JSON</button>
          <input id="timeline-json-file" class="visually-hidden" type="file" accept="application/json,.json" />
        </div>
      </div>
      <div class="preview-output" aria-live="off"></div>
      <label class="preview-seek-label" for="preview-seek">
        Preview time
        <span>Drag to inspect a specific millisecond. Scrubbing pauses playback.</span>
        <input id="preview-seek" type="range" min="0" max="0" step="1" value="0" disabled />
      </label>
      <p class="preview-status"></p>
      <p class="share-status" aria-live="polite"></p>
    </section>
    <ol class="timeline"></ol>
  </section>
`;

const input = requireElement<HTMLTextAreaElement>("#animation-input", app);
const timingScaleInput = requireElement<HTMLInputElement>("#timing-scale", app);
const maxInputCharacters = requireElement<HTMLInputElement>("#max-input-characters", app);
const maxLines = requireElement<HTMLInputElement>("#max-lines", app);
const maxFrames = requireElement<HTMLInputElement>("#max-frames", app);
const maxOutputCharacters = requireElement<HTMLInputElement>("#max-output-characters", app);
const summary = requireElement<HTMLElement>(".summary", app);
const autoplayPreview = requireElement<HTMLInputElement>("#autoplay-preview", app);
const restartPreviewButton = requireElement<HTMLButtonElement>("#restart-preview", app);
const copyShareUrlButton = requireElement<HTMLButtonElement>("#copy-share-url", app);
const importTimelineJsonButton = requireElement<HTMLButtonElement>("#import-timeline-json", app);
const timelineJsonFileInput = requireElement<HTMLInputElement>("#timeline-json-file", app);
const copyTimelineJsonButton = requireElement<HTMLButtonElement>("#copy-timeline-json", app);
const previewOutput = requireElement<HTMLElement>(".preview-output", app);
const previewSeekInput = requireElement<HTMLInputElement>("#preview-seek", app);
const previewStatus = requireElement<HTMLElement>(".preview-status", app);
const shareStatus = requireElement<HTMLElement>(".share-status", app);
const timeline = requireElement<HTMLOListElement>(".timeline", app);

const initialState = readInitialState();
input.value = initialState.text;
timingScaleInput.value = initialState.timingScale.toString();
maxInputCharacters.value = initialState.maxInputCharacters.toString();
maxLines.value = initialState.maxLines.toString();
maxFrames.value = initialState.maxFrames.toString();
maxOutputCharacters.value = initialState.maxOutputCharacters.toString();

let previewFrames: TimelineFrame[] = [];
let previewTotalMilliseconds: number | null = null;
let previewStartedAt = performance.now();
let previewCurrentMilliseconds = 0;
let previewAnimationFrame: number | null = null;

function readInitialState(): typeof defaultLimits & { text: string; timingScale: number } {
  const params = new URLSearchParams(window.location.search);

  return {
    text: params.get("text") ?? defaultAnimation,
    timingScale: readPositiveParameter(params, "scale", defaultTimingScale),
    maxInputCharacters: readPositiveParameter(params, "maxInputCharacters", defaultLimits.maxInputCharacters),
    maxLines: readPositiveParameter(params, "maxLines", defaultLimits.maxLines),
    maxFrames: readPositiveParameter(params, "maxFrames", defaultLimits.maxFrames),
    maxOutputCharacters: readPositiveParameter(params, "maxOutputCharacters", defaultLimits.maxOutputCharacters)
  };
}

function readPositiveParameter(params: URLSearchParams, name: string, fallback: number): number {
  const value = params.get(name);

  if (value === null) {
    return fallback;
  }

  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed >= 1 ? parsed : fallback;
}

function writeUrlState(): void {
  const params = new URLSearchParams();
  setParamIfChanged(params, "text", input.value, defaultAnimation);
  setParamIfChanged(params, "scale", timingScaleInput.value, defaultTimingScale.toString());
  setParamIfChanged(params, "maxInputCharacters", maxInputCharacters.value, defaultLimits.maxInputCharacters.toString());
  setParamIfChanged(params, "maxLines", maxLines.value, defaultLimits.maxLines.toString());
  setParamIfChanged(params, "maxFrames", maxFrames.value, defaultLimits.maxFrames.toString());
  setParamIfChanged(params, "maxOutputCharacters", maxOutputCharacters.value, defaultLimits.maxOutputCharacters.toString());

  const query = params.toString();
  const nextUrl = `${window.location.pathname}${query.length === 0 ? "" : `?${query}`}${window.location.hash}`;
  window.history.replaceState(null, "", nextUrl);
}

function setParamIfChanged(params: URLSearchParams, name: string, value: string, defaultValue: string): void {
  if (value !== defaultValue) {
    params.set(name, value);
  }
}

function renderTimeline(): void {
  writeUrlState();
  shareStatus.textContent = "";

  const timingScale = readPositiveInteger(timingScaleInput);
  if (timingScale === null) {
    renderError("timingScale must be a positive whole number.");
    return;
  }

  const limits = readLimits();

  if (typeof limits === "string") {
    renderError(limits);
    return;
  }

  const result = api.createAnimationTimelineWithSafetyLimits(
    input.value,
    timingScale,
    limits.maxInputCharacters,
    limits.maxLines,
    limits.maxFrames,
    limits.maxOutputCharacters
  );

  if (!result.isSuccess) {
    renderError(result.errors.map((error) => `Line ${error.lineNumber}: ${error.message}`).join("\n"));
    return;
  }

  const totalMilliseconds = result.totalMilliseconds ?? null;

  summary.className = "summary";
  summary.textContent = `${result.frames.length} frame${result.frames.length === 1 ? "" : "s"}${totalMilliseconds === null ? "" : `, ${totalMilliseconds}ms total`}`;

  updatePreview(result.frames, totalMilliseconds);

  timeline.replaceChildren(
    ...result.frames.map((frame) => {
      const item = document.createElement("li");
      const end = frame.endMilliseconds == null ? "∞" : `${frame.endMilliseconds}ms`;
      item.innerHTML = `
        <span class="time">${frame.startMilliseconds}ms → ${end}</span>
        <span class="text"></span>
      `;
      requireElement<HTMLElement>(".text", item).append(renderLegacySegments(frame.text));
      return item;
    })
  );
}

function renderLegacySegments(text: string): DocumentFragment {
  const fragment = document.createDocumentFragment();

  api.renderLegacyText(text).forEach((segment) => {
    const span = document.createElement("span");
    span.textContent = segment.text;

    if (segment.color != null) {
      span.style.color = segment.color;
    }

    if (segment.bold) {
      span.style.fontWeight = "700";
    }

    if (segment.italic) {
      span.style.fontStyle = "italic";
    }

    const decorations = [];
    if (segment.underlined) {
      decorations.push("underline");
    }
    if (segment.strikethrough) {
      decorations.push("line-through");
    }
    if (decorations.length > 0) {
      span.style.textDecoration = decorations.join(" ");
    }

    if (segment.obfuscated) {
      span.classList.add("obfuscated");
    }

    fragment.append(span);
  });

  return fragment;
}

function updatePreview(frames: TimelineFrame[], totalMilliseconds?: number | null): void {
  previewFrames = frames;
  previewTotalMilliseconds = totalMilliseconds ?? null;
  copyTimelineJsonButton.disabled = previewFrames.length === 0;
  configurePreviewSeek();
  restartPreview();
}

function restartPreview(): void {
  previewStartedAt = performance.now();
  renderPreviewAt(0);
  schedulePreview();
}

function schedulePreview(): void {
  cancelScheduledPreview();

  if (autoplayPreview.checked && previewFrames.length > 1) {
    previewAnimationFrame = requestAnimationFrame(tickPreview);
  }
}

function cancelScheduledPreview(): void {
  if (previewAnimationFrame !== null) {
    cancelAnimationFrame(previewAnimationFrame);
    previewAnimationFrame = null;
  }
}

function tickPreview(now: number): void {
  renderPreviewAt(previewTime(now));

  if (autoplayPreview.checked && previewFrames.length > 1) {
    previewAnimationFrame = requestAnimationFrame(tickPreview);
  }
}

function previewTime(now: number): number {
  const elapsed = Math.max(0, Math.floor(now - previewStartedAt));

  if (previewTotalMilliseconds !== null && previewTotalMilliseconds > 0) {
    return elapsed % previewTotalMilliseconds;
  }

  return elapsed;
}

function renderPreviewAt(milliseconds: number): void {
  previewCurrentMilliseconds = Math.max(0, Math.floor(milliseconds));
  const frame = findFrame(milliseconds);

  if (frame === null) {
    previewOutput.textContent = "";
    previewStatus.textContent = "No preview frames";
    return;
  }

  previewOutput.replaceChildren(renderLegacySegments(frame.text));
  previewStatus.textContent = `${previewCurrentMilliseconds}ms${previewTotalMilliseconds === null ? "" : ` / ${previewTotalMilliseconds}ms`}`;
  if (!previewSeekInput.disabled) {
    previewSeekInput.value = Math.min(previewCurrentMilliseconds, Number(previewSeekInput.max)).toString();
  }
}

function findFrame(milliseconds: number): TimelineFrame | null {
  const activeFrame = previewFrames.find((frame) =>
    frame.startMilliseconds <= milliseconds &&
    (frame.endMilliseconds == null || milliseconds < frame.endMilliseconds)
  );

  if (activeFrame !== undefined) {
    return activeFrame;
  }

  for (let index = previewFrames.length - 1; index >= 0; index -= 1) {
    if (previewFrames[index].startMilliseconds <= milliseconds) {
      return previewFrames[index];
    }
  }

  return previewFrames[0] ?? null;
}

function configurePreviewSeek(): void {
  const maximum = previewSeekMaximum();
  previewCurrentMilliseconds = 0;
  previewSeekInput.value = "0";
  previewSeekInput.max = maximum.toString();
  previewSeekInput.disabled = maximum <= 0;
}

function previewSeekMaximum(): number {
  if (previewFrames.length === 0) {
    return 0;
  }

  if (previewTotalMilliseconds !== null && previewTotalMilliseconds > 0) {
    return previewTotalMilliseconds;
  }

  return Math.max(
    ...previewFrames.map((frame) => frame.endMilliseconds ?? frame.startMilliseconds)
  );
}

function readLimits():
  | typeof defaultLimits
  | string {
  const limits = {
    maxInputCharacters: readPositiveInteger(maxInputCharacters),
    maxLines: readPositiveInteger(maxLines),
    maxFrames: readPositiveInteger(maxFrames),
    maxOutputCharacters: readPositiveInteger(maxOutputCharacters)
  };

  const invalidLimit = Object.entries(limits).find(([, value]) => value === null);
  if (invalidLimit !== undefined) {
    return `${invalidLimit[0]} must be a positive whole number.`;
  }

  return limits as typeof defaultLimits;
}

function readPositiveInteger(element: HTMLInputElement): number | null {
  const value = element.valueAsNumber;
  return Number.isInteger(value) && value >= 1 ? value : null;
}

function renderError(message: string): void {
  summary.className = "summary error";
  summary.textContent = message;
  cancelScheduledPreview();
  previewFrames = [];
  previewTotalMilliseconds = null;
  previewCurrentMilliseconds = 0;
  previewSeekInput.value = "0";
  previewSeekInput.max = "0";
  previewSeekInput.disabled = true;
  copyTimelineJsonButton.disabled = true;
  previewOutput.textContent = "";
  previewStatus.textContent = "Fix the input above to restart the preview.";
  shareStatus.textContent = "";
  timeline.replaceChildren();
}

async function copyShareUrl(): Promise<void> {
  writeUrlState();
  await copyText(window.location.href, "Share link copied.");
}

async function copyTimelineJson(): Promise<void> {
  if (previewFrames.length === 0) {
    shareStatus.textContent = "No timeline frames are available to copy.";
    return;
  }

  const limits = readLimits();
  if (typeof limits === "string") {
    shareStatus.textContent = limits;
    return;
  }

  const timingScale = readPositiveInteger(timingScaleInput);
  if (timingScale === null) {
    shareStatus.textContent = "timingScale must be a positive whole number.";
    return;
  }

  await copyText(
    JSON.stringify({
      text: input.value,
      timingScale,
      limits,
      totalMilliseconds: previewTotalMilliseconds,
      frames: previewFrames
    }, null, 2),
    "Timeline JSON copied."
  );
}

async function importTimelineJson(file: File | undefined): Promise<void> {
  if (file === undefined) {
    return;
  }

  try {
    applyTimelineJson(JSON.parse(await file.text()) as TimelineJsonExport);
    shareStatus.textContent = "Timeline JSON imported.";
  } catch (error) {
    shareStatus.textContent = `Could not import timeline JSON: ${error instanceof Error ? error.message : String(error)}`;
  } finally {
    timelineJsonFileInput.value = "";
  }
}

function applyTimelineJson(data: TimelineJsonExport): void {
  if (typeof data.text !== "string") {
    throw new Error("Missing text field.");
  }

  input.value = data.text;
  timingScaleInput.value = positiveIntegerOrDefault(data.timingScale, defaultTimingScale).toString();

  const limits = data.limits ?? {};
  maxInputCharacters.value = positiveIntegerOrDefault(limits.maxInputCharacters, defaultLimits.maxInputCharacters).toString();
  maxLines.value = positiveIntegerOrDefault(limits.maxLines, defaultLimits.maxLines).toString();
  maxFrames.value = positiveIntegerOrDefault(limits.maxFrames, defaultLimits.maxFrames).toString();
  maxOutputCharacters.value = positiveIntegerOrDefault(limits.maxOutputCharacters, defaultLimits.maxOutputCharacters).toString();

  renderTimeline();
}

function positiveIntegerOrDefault(value: unknown, fallback: number): number {
  return typeof value === "number" && Number.isInteger(value) && value >= 1 ? value : fallback;
}

async function copyText(text: string, successMessage: string): Promise<void> {
  if (navigator.clipboard === undefined) {
    shareStatus.textContent = "Clipboard access is unavailable in this browser.";
    return;
  }

  try {
    await navigator.clipboard.writeText(text);
    shareStatus.textContent = successMessage;
  } catch (error) {
    shareStatus.textContent = `Could not copy: ${error instanceof Error ? error.message : String(error)}`;
  }
}

const timelineControls: HTMLElement[] = [input, timingScaleInput, maxInputCharacters, maxLines, maxFrames, maxOutputCharacters];
timelineControls.forEach((control) => control.addEventListener("input", renderTimeline));
autoplayPreview.addEventListener("change", () => {
  if (autoplayPreview.checked) {
    previewStartedAt = performance.now() - previewCurrentMilliseconds;
    schedulePreview();
  } else {
    cancelScheduledPreview();
  }
});
restartPreviewButton.addEventListener("click", restartPreview);
copyShareUrlButton.addEventListener("click", () => {
  void copyShareUrl();
});
importTimelineJsonButton.addEventListener("click", () => {
  timelineJsonFileInput.click();
});
timelineJsonFileInput.addEventListener("change", () => {
  void importTimelineJson(timelineJsonFileInput.files?.[0]);
});
copyTimelineJsonButton.addEventListener("click", () => {
  void copyTimelineJson();
});
previewSeekInput.addEventListener("input", () => {
  const milliseconds = Math.max(0, Math.floor(previewSeekInput.valueAsNumber));
  autoplayPreview.checked = false;
  cancelScheduledPreview();
  renderPreviewAt(milliseconds);
});
renderTimeline();
