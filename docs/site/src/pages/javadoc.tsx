import Layout from "@theme/Layout";
import useBaseUrl from "@docusaurus/useBaseUrl";

export default function JavaDocPage() {
  return (
    <Layout title="JavaDoc" description="TitleManager JavaDoc API reference">
      <main className="tm-api-frame-page">
        <header className="tm-api-frame-page__header">
          <h1>JavaDoc</h1>
          <p>
            Java-oriented API reference generated from <code>titlemanager-bukkit-api</code>.
          </p>
          <a href={useBaseUrl("/api/javadoc/index.html")}>Open JavaDoc in a separate page</a>
        </header>
        <iframe
          className="tm-api-frame-page__frame"
          src={useBaseUrl("/api/javadoc/index.html")}
          title="TitleManager JavaDoc"
        />
      </main>
    </Layout>
  );
}
