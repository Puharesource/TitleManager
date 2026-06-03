import { createRoot } from "react-dom/client";
import AnimationPreview from "./components/AnimationPreview";
import "./css/custom.css";

function PreviewApp() {
  return (
    <main className="container margin-vert--lg">
      <h1>TitleManager animation preview</h1>
      <p>
        This Vite/Rolldown/Oxc entry verifies the same React preview component used by the
        Docusaurus docs.
      </p>
      <AnimationPreview
        title="shine"
        scale={50}
        text="${shine:[0;2;0][0;25;0][0;25;0][&3;&b]My Server}"
      />
    </main>
  );
}

const root = document.getElementById("root");

if (root === null) {
  throw new Error("Missing root element");
}

createRoot(root).render(<PreviewApp />);
