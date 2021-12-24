import { defineNuxtConfig } from 'nuxt3'

export default defineNuxtConfig({
  meta: {
    link: [
      { rel: 'preconnect', href: 'https://fonts.googleapis.com' },
      { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: true },
      { rel: 'stylesheet', href: 'https://fonts.googleapis.com/css2?family=Quicksand:wght@300;400;500;600;700&display=swap' },
      { rel: 'stylesheet', href: 'https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap' },
      { rel: 'stylesheet', href: 'https://fonts.googleapis.com/css2?family=JetBrains+Mono&display=swap' }
    ],
    script: [
      { src: '/generated/titlemanager.js' }
    ]
  },

  buildModules: [
    '@vueuse/nuxt',
    'nuxt-windicss'
  ],

  css: [
    '~/assets/style/style.css',
    '~/assets/style/mc.css'
  ]
})
