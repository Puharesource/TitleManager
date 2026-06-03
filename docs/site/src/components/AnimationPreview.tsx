import type { ReactNode } from "react";
import { useEffect, useMemo, useRef, useState } from "react";
import { TitleManagerCoreApi } from "@titlemanager/core";

type TimelineFrame = {
  startMilliseconds: number;
  endMilliseconds?: number | null;
  text: string;
};

type HslColor = {
  h: number;
  s: number;
  l: number;
};

type AnimationPreviewProps = {
  title?: string;
  text?: string;
  scale?: number;
  kind?: "timeline" | "cycle" | "gradient" | "cgradient";
  label?: string;
  gradient?: string;
  precision?: number;
  separator?: string;
  preset?: "left-to-right" | "right-to-left";
  autoplay?: boolean;
};

const api = TitleManagerCoreApi.getInstance();

const defaultLimits = {
  maxInputCharacters: 10_000,
  maxLines: 500,
  maxFrames: 2_000,
  maxOutputCharacters: 100_000,
};

const defaultGradient = ["#833AB4", "#FD1D1D", "#FCB045"];
const defaultGradientPrecision = 80;

const animationPresets: Record<NonNullable<AnimationPreviewProps["preset"]>, string> = {
  "left-to-right": [
    "[0;5;0]&7&b-&7---------",
    "[0;2;0]&7-&b-&7--------",
    "[0;2;0]&7--&b-&7-------",
    "[0;2;0]&7---&b-&7------",
    "[0;2;0]&7----&b-&7-----",
    "[0;2;0]&7-----&b-&7----",
    "[0;2;0]&7------&b-&7---",
    "[0;2;0]&7-------&b-&7--",
    "[0;2;0]&7--------&b-&7-",
    "[0;5;0]&7---------&b-&7",
    "[0;2;0]&7--------&b-&7-",
    "[0;2;0]&7-------&b-&7--",
    "[0;2;0]&7------&b-&7---",
    "[0;2;0]&7-----&b-&7----",
    "[0;2;0]&7----&b-&7-----",
    "[0;2;0]&7---&b-&7------",
    "[0;2;0]&7--&b-&7-------",
    "[0;2;0]&7-&b-&7--------",
  ].join("\n"),
  "right-to-left": [
    "[0;5;0]&7---------&b-&7",
    "[0;2;0]&7--------&b-&7-",
    "[0;2;0]&7-------&b-&7--",
    "[0;2;0]&7------&b-&7---",
    "[0;2;0]&7-----&b-&7----",
    "[0;2;0]&7----&b-&7-----",
    "[0;2;0]&7---&b-&7------",
    "[0;2;0]&7--&b-&7-------",
    "[0;2;0]&7-&b-&7--------",
    "[0;5;0]&7&b-&7---------",
    "[0;2;0]&7-&b-&7--------",
    "[0;2;0]&7--&b-&7-------",
    "[0;2;0]&7---&b-&7------",
    "[0;2;0]&7----&b-&7-----",
    "[0;2;0]&7-----&b-&7----",
    "[0;2;0]&7------&b-&7---",
    "[0;2;0]&7-------&b-&7--",
    "[0;2;0]&7--------&b-&7-",
  ].join("\n"),
};

export default function AnimationPreview({
  title,
  text = "",
  scale = 50,
  kind = "timeline",
  label = "████████",
  gradient,
  precision = defaultGradientPrecision,
  separator = "",
  preset,
  autoplay = true,
}: AnimationPreviewProps) {
  const frames = useMemo(
    () => createFrames({ text, scale, kind, label, gradient, precision, separator, preset }),
    [text, scale, kind, label, gradient, precision, separator, preset],
  );
  const totalMilliseconds = useMemo(() => calculateTotalMilliseconds(frames), [frames]);
  const [elapsedMilliseconds, setElapsedMilliseconds] = useState(0);
  const startedAt = useRef(0);

  useEffect(() => {
    startedAt.current = performance.now();
    setElapsedMilliseconds(0);

    if (!autoplay || frames.length <= 1) {
      return undefined;
    }

    let request = 0;
    const tick = (now: number) => {
      setElapsedMilliseconds(previewTime(now, startedAt.current, totalMilliseconds));
      request = requestAnimationFrame(tick);
    };

    request = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(request);
  }, [autoplay, frames, totalMilliseconds]);

  const activeFrame = findFrame(frames, elapsedMilliseconds);

  return (
    <div className="tm-animation-preview">
      {title ? <div className="tm-animation-preview__title">{title}</div> : null}
      <div className="tm-animation-preview__output">
        {activeFrame ? renderLegacySegments(activeFrame.text) : "No preview frames"}
      </div>
      <div className="tm-animation-preview__status">
        {totalMilliseconds === null
          ? `${elapsedMilliseconds}ms`
          : `${elapsedMilliseconds}ms / ${totalMilliseconds}ms`}
      </div>
    </div>
  );
}

function createFrames(
  props: Required<
    Pick<AnimationPreviewProps, "text" | "scale" | "kind" | "label" | "precision" | "separator">
  > &
    Pick<AnimationPreviewProps, "gradient" | "preset">,
): TimelineFrame[] {
  if (props.kind === "cycle") {
    return createCycleFrames(props);
  }
  if (props.kind === "gradient" || props.kind === "cgradient") {
    return createGradientFrames(props, props.kind === "cgradient");
  }

  const animationText = props.preset === undefined ? props.text : animationPresets[props.preset];
  const result = api.createAnimationTimelineWithSafetyLimits(
    animationText,
    props.scale,
    defaultLimits.maxInputCharacters,
    defaultLimits.maxLines,
    defaultLimits.maxFrames,
    defaultLimits.maxOutputCharacters,
  );

  if (!result.isSuccess) {
    return [
      {
        startMilliseconds: 0,
        endMilliseconds: null,
        text: result.errors.map((error) => error.message).join("\n"),
      },
    ];
  }

  return Array.from(result.frames, (frame) => ({
    startMilliseconds: frame.startMilliseconds,
    endMilliseconds: frame.endMilliseconds,
    text: frame.text,
  }));
}

function createCycleFrames(
  props: Pick<AnimationPreviewProps, "gradient" | "label" | "precision">,
): TimelineFrame[] {
  const colors = gradientColors(props.gradient, true, props.precision ?? defaultGradientPrecision);
  const label = props.label ?? "████████";

  return colors.map((color, index) => ({
    startMilliseconds: index * 50,
    endMilliseconds: (index + 1) * 50,
    text: `${api.legacyRgbIntColorCode(color)}${label}`,
  }));
}

function createGradientFrames(
  props: Pick<AnimationPreviewProps, "gradient" | "precision" | "separator" | "text">,
  clamped: boolean,
): TimelineFrame[] {
  const text = props.text ?? "Gradient text";
  const precision = clamped
    ? Array.from(text).length + 1
    : (props.precision ?? defaultGradientPrecision);
  const colors = gradientColors(props.gradient, true, precision);

  return colors.map((_, frameIndex) => ({
    startMilliseconds: frameIndex * 50,
    endMilliseconds: (frameIndex + 1) * 50,
    text: colorText(text, props.separator ?? "", colors, frameIndex),
  }));
}

function gradientColors(
  gradient: string | undefined,
  radial: boolean,
  precision: number,
): number[] {
  const base = parseGradient(gradient);
  const colors = radial ? [...base, ...base.slice(0, -1).reverse()] : base;
  const result: number[] = [];

  for (let index = 0; index < precision; index += 1) {
    result.push(hslToRgb(interpolateHsl(colors, index / precision, false)));
  }

  return result;
}

function colorText(text: string, separator: string, colors: number[], frameIndex: number): string {
  return Array.from(text)
    .map(
      (character, textIndex) =>
        `${api.legacyRgbIntColorCode(colors[(frameIndex + textIndex) % colors.length])}${separator}${character}`,
    )
    .join("");
}

function parseGradient(value: string | undefined): HslColor[] {
  const colors = (value ?? defaultGradient.join(","))
    .split(",")
    .map((color) => color.trim())
    .filter((color) => color.length > 0)
    .map((color) => rgbToHsl(api.parseLegacyHexColorRgb(color)));

  return colors.length >= 2
    ? colors
    : defaultGradient.map((color) => rgbToHsl(api.parseLegacyHexColorRgb(color)));
}

function interpolateHsl(colors: HslColor[], position: number, longHue: boolean): HslColor {
  const normalizedPosition = Math.min(1, Math.max(0, Number.isFinite(position) ? position : 0));
  const segment = 1 / (colors.length - 1);
  const segmentIndex = Math.min(
    colors.length - 2,
    Math.floor(normalizedPosition * (colors.length - 1)),
  );
  const start = colors[segmentIndex];
  const end = colors[segmentIndex + 1];
  const t = (normalizedPosition - segmentIndex * segment) / segment;

  return {
    h: interpolateHue(start.h, end.h, t, longHue),
    s: lerp(start.s, end.s, t),
    l: lerp(start.l, end.l, t),
  };
}

function interpolateHue(start: number, end: number, position: number, longHue: boolean): number {
  let startDegrees = start * 360;
  let endDegrees = end * 360;
  const distance = Math.abs(endDegrees - startDegrees);

  if (longHue ? distance < 180 : distance > 180) {
    if (end < start) {
      startDegrees += 360;
    } else {
      endDegrees += 360;
    }
  }

  return modulo(lerp(startDegrees, endDegrees, position), 360) / 360;
}

function rgbToHsl(rgb: number): HslColor {
  const red = ((rgb >> 16) & 0xff) / 255;
  const green = ((rgb >> 8) & 0xff) / 255;
  const blue = (rgb & 0xff) / 255;
  const max = Math.max(red, green, blue);
  const min = Math.min(red, green, blue);
  const lightness = (max + min) / 2;

  if (max === min) {
    return { h: 0, s: 0, l: lightness };
  }

  const delta = max - min;
  const saturation = delta / (1 - Math.abs(2 * lightness - 1));
  const hue =
    max === red
      ? modulo((green - blue) / delta, 6) / 6
      : max === green
        ? ((blue - red) / delta + 2) / 6
        : ((red - green) / delta + 4) / 6;

  return { h: hue, s: saturation, l: lightness };
}

function hslToRgb(color: HslColor): number {
  const hue = modulo(color.h, 1);
  const saturation = Math.min(1, Math.max(0, color.s));
  const lightness = Math.min(1, Math.max(0, color.l));
  let red: number;
  let green: number;
  let blue: number;

  if (saturation === 0) {
    red = lightness;
    green = lightness;
    blue = lightness;
  } else {
    const q =
      lightness < 0.5
        ? lightness * (1 + saturation)
        : lightness + saturation - lightness * saturation;
    const p = 2 * lightness - q;

    red = hueToRgb(p, q, hue + 1 / 3);
    green = hueToRgb(p, q, hue);
    blue = hueToRgb(p, q, hue - 1 / 3);
  }

  return (toRgbChannel(red) << 16) | (toRgbChannel(green) << 8) | toRgbChannel(blue);
}

function hueToRgb(p: number, q: number, hue: number): number {
  const normalizedHue = modulo(hue, 1);

  if (normalizedHue < 1 / 6) {
    return p + (q - p) * 6 * normalizedHue;
  }
  if (normalizedHue < 1 / 2) {
    return q;
  }
  if (normalizedHue < 2 / 3) {
    return p + (q - p) * (2 / 3 - normalizedHue) * 6;
  }

  return p;
}

function toRgbChannel(value: number): number {
  return Math.min(255, Math.max(0, Math.round(value * 255)));
}

function lerp(start: number, end: number, position: number): number {
  if (position <= 0) {
    return start;
  }
  if (position >= 1) {
    return end;
  }

  return start + position * (end - start);
}

function modulo(value: number, divisor: number): number {
  return ((value % divisor) + divisor) % divisor;
}

function calculateTotalMilliseconds(frames: TimelineFrame[]): number | null {
  const lastFrame = frames[frames.length - 1];
  return lastFrame?.endMilliseconds ?? null;
}

function previewTime(now: number, startedAt: number, totalMilliseconds: number | null): number {
  const elapsed = Math.max(0, Math.floor(now - startedAt));
  return totalMilliseconds !== null && totalMilliseconds > 0
    ? elapsed % totalMilliseconds
    : elapsed;
}

function findFrame(frames: TimelineFrame[], milliseconds: number): TimelineFrame | null {
  const activeFrame = frames.find(
    (frame) =>
      frame.startMilliseconds <= milliseconds &&
      (frame.endMilliseconds == null || milliseconds < frame.endMilliseconds),
  );

  if (activeFrame !== undefined) {
    return activeFrame;
  }

  for (let index = frames.length - 1; index >= 0; index -= 1) {
    if (frames[index].startMilliseconds <= milliseconds) {
      return frames[index];
    }
  }

  return frames[0] ?? null;
}

function renderLegacySegments(text: string): ReactNode[] {
  return api.renderLegacyText(text).map((segment, index) => {
    const decorations = [];
    if (segment.underlined) {
      decorations.push("underline");
    }
    if (segment.strikethrough) {
      decorations.push("line-through");
    }

    return (
      <span
        className={segment.obfuscated ? "tm-animation-preview__obfuscated" : undefined}
        key={index}
        style={{
          color: segment.color ?? undefined,
          fontWeight: segment.bold ? 700 : undefined,
          fontStyle: segment.italic ? "italic" : undefined,
          textDecoration: decorations.length > 0 ? decorations.join(" ") : undefined,
        }}
      >
        {segment.text}
      </span>
    );
  });
}
