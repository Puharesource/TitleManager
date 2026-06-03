import Layout from "@theme/Layout";
import useBaseUrl from "@docusaurus/useBaseUrl";

export default function DokkaPage() {
  return (
    <Layout title="Dokka Kotlin" description="TitleManager Dokka Kotlin API reference">
      <main className="tm-api-frame-page">
        <header className="tm-api-frame-page__header">
          <h1>Dokka Kotlin</h1>
          <p>
            Kotlin-oriented API reference generated from <code>titlemanager-bukkit-api</code>.
          </p>
          <a href={useBaseUrl("/api/kotlin/index.html")}>
            Open Dokka Kotlin docs in a separate page
          </a>
        </header>
        <iframe
          className="tm-api-frame-page__frame"
          src={useBaseUrl("/api/kotlin/index.html")}
          title="TitleManager Dokka Kotlin documentation"
        />
      </main>
    </Layout>
  );
}
