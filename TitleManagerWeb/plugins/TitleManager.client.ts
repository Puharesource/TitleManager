import { defineNuxtPlugin } from '#app'
import TitleManagerDefs from 'generated/TitleManager-TitleManagerLib'

type TitleManagerLibType = typeof TitleManagerDefs
const { TitleManagerLib } = window as any as { TitleManagerLib: TitleManagerLibType }

declare module '#app' {
  interface NuxtApp {
    $titlemanager: TitleManagerLibType
  }
}

declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    $titlemanager: TitleManagerLibType
  }
}

export default defineNuxtPlugin((nuxtApp) => {
  console.log('providing $titlemanager', TitleManagerLib)

  return {
    provide: {
      titlemanager: TitleManagerLib
    }
  }
})
