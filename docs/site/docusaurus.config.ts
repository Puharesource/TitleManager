import type { Config } from "@docusaurus/types";
import type { Options as PresetOptions } from "@docusaurus/preset-classic";
import { themes as prismThemes } from "prism-react-renderer";
import path from "node:path";

const config: Config = {
  title: "TitleManager",
  tagline:
    "Animated titles, actionbars, tab lists, scoreboards, gradients, and placeholders for Bukkit/Paper servers.",
  favicon: "img/titlemanager_logo.svg",
  url: "https://titlemanager.tarkan.dev",
  baseUrl: "/",
  organizationName: "Puharesource",
  projectName: "TitleManager",
  onBrokenLinks: "throw",
  onDuplicateRoutes: "throw",
  trailingSlash: false,
  i18n: {
    defaultLocale: "en",
    locales: ["en"],
  },
  presets: [
    [
      "classic",
      {
        docs: {
          path: "docs",
          routeBasePath: "/",
          sidebarPath: "./sidebars.ts",
          editUrl: "https://github.com/Puharesource/TitleManager/edit/master/docs/site/",
          showLastUpdateAuthor: false,
          showLastUpdateTime: true,
        },
        blog: false,
        theme: {
          customCss: "./src/css/custom.css",
        },
        sitemap: {
          changefreq: "weekly",
          priority: 0.5,
        },
      } satisfies PresetOptions,
    ],
  ],
  markdown: {
    hooks: {
      onBrokenMarkdownLinks: "warn",
    },
    mdx1Compat: {
      comments: false,
      admonitions: false,
      headingIds: false,
    },
  },
  themeConfig: {
    image: "img/titlemanager_logo_outline.svg",
    navbar: {
      title: "TitleManager",
      logo: {
        alt: "TitleManager logo",
        src: "img/titlemanager_logo.svg",
      },
      items: [
        { type: "docSidebar", sidebarId: "docsSidebar", position: "left", label: "Docs" },
        {
          type: "dropdown",
          label: "API",
          position: "right",
          items: [
            { label: "API Reference", to: "/api-reference" },
            { label: "JavaDoc", to: "/javadoc" },
            { label: "Dokka Kotlin", to: "/dokka" },
          ],
        },
        {
          href: "https://github.com/Puharesource/TitleManager",
          label: "GitHub",
          position: "right",
        },
        {
          href: "https://github.com/Puharesource/TitleManager/releases",
          label: "Download",
          position: "right",
        },
      ],
    },
    footer: {
      style: "dark",
      links: [
        {
          title: "Project",
          items: [
            { label: "Source Code", href: "https://github.com/Puharesource/TitleManager" },
            { label: "JavaDoc", href: "https://titlemanager.tarkan.dev/javadoc" },
            {
              label: "License",
              href: "https://github.com/Puharesource/TitleManager/blob/master/LICENSE.md",
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} TitleManager contributors.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
    },
  },
  plugins: [
    function titleManagerCoreAliasPlugin() {
      return {
        name: "titlemanager-core-alias",
        configureWebpack() {
          return {
            resolve: {
              alias: {
                "@titlemanager/core": path.resolve(__dirname, ".generated/titlemanager-core"),
              },
            },
          };
        },
      };
    },
  ],
};

export default config;
