import { useMemo, useState } from "react";
import AnimationPreview from "./AnimationPreview";

const examples = [
  {
    label: "Shine",
    text: "${shine:[0;2;0][0;25;0][0;25;0][&3;&b]My Server}",
  },
  {
    label: "Marquee",
    text: "${marquee:[0;2;0][20]Scrolling text}",
  },
  {
    label: "Text write",
    text: "${text_write:Typed text}",
  },
  {
    label: "Countdown",
    text: "${countdown:10}",
  },
  {
    label: "Multiline",
    text: "[0;20;0]&bWelcome\n[0;20;0]&7Enjoy your stay",
  },
];

export default function AnimationPlayground() {
  const [text, setText] = useState(examples[0].text);
  const [scale, setScale] = useState(50);
  const lineCount = useMemo(() => text.split("\n").length, [text]);
  const characterCount = text.length;

  return (
    <section className="tm-animation-playground">
      <div className="tm-animation-playground__controls">
        <label className="tm-animation-playground__field">
          <span>Animation text</span>
          <textarea
            value={text}
            rows={8}
            spellCheck={false}
            onChange={(event) => setText(event.target.value)}
          />
        </label>

        <label className="tm-animation-playground__field">
          <span>Timing scale</span>
          <input
            type="number"
            min={1}
            max={1000}
            value={scale}
            onChange={(event) => setScale(clampScale(event.target.valueAsNumber))}
          />
        </label>

        <div className="tm-animation-playground__examples" aria-label="Example animations">
          {examples.map((example) => (
            <button key={example.label} type="button" onClick={() => setText(example.text)}>
              {example.label}
            </button>
          ))}
        </div>

        <p className="tm-animation-playground__meta">
          {characterCount} characters · {lineCount} {lineCount === 1 ? "line" : "lines"}
        </p>
      </div>

      <AnimationPreview title="Live preview" text={text} scale={scale} />
    </section>
  );
}

function clampScale(value: number): number {
  if (!Number.isFinite(value)) {
    return 50;
  }

  return Math.min(1000, Math.max(1, Math.trunc(value)));
}
