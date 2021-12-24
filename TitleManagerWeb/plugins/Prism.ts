import { defineNuxtPlugin } from '#app'
import Prism from 'prismjs'
import 'prismjs/components/prism-yaml.min'
import 'prism-themes/themes/prism-darcula.css'

declare module '#app' {
  interface NuxtApp {
    $prism: typeof Prism
  }
}

declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    $prism: typeof Prism
  }
}

export default defineNuxtPlugin(() => {
  return {
    provide: {
      prism: Prism
    }
  }
})
