<template>
  <div class="titlemanager-site">
    <div class="py-4 mb-6">
      <div class="container flex items-center justify-between gap-4">
        <nuxt-link to="/" class="flex items-center gap-4 flex-1">
          <div style="overflow: hidden;">
            <tm-image :src="logoImage" min-width="50px" min-height="50px" max-height="50px" max-width="50px" />
          </div>

          <div class="text-xl">
            <div v-if="isServer" class="mc-font">
              TitleManager
            </div>
            <tm-minecraft-text v-else :text="logoText" />
          </div>
        </nuxt-link>

        <div class="flex gap-8 flex-1 font-serif font-semibold">
          <nuxt-link to="/features" class="underline-item">Features</nuxt-link>
          <nuxt-link to="/documentation" class="underline-item">Documentation</nuxt-link>
          <nuxt-link to="/config" class="underline-item">Configuration</nuxt-link>
          <nuxt-link to="/generator" class="underline-item">Generator</nuxt-link>
        </div>

        <div class="flex gap-4 items-center justify-end flex-1">
          <a href="https://github.com/Puharesource/TitleManager" class="hover:text-[#ff8457] transition-colors">
            <icon icon="akar-icons:github-fill" class="-mb-1 text-xl" />
          </a>
          <a class="filter grayscale-100 hover:grayscale-0 transition-all" href="https://www.spigotmc.org/resources/titlemanager.1049/">
            <tm-image :src="spigotImage" class="inline-block mt-2" min-width="20px" min-height="20px" max-height="20px" max-width="20px" />
          </a>
          <a href="https://github.com/Puharesource/TitleManager/releases/tag/2.3.6" class="py-2 px-4 bg-[#ff8457] text-white hover:bg-white border-2 border-[#ff8457] text-sm uppercase hover:text-black transition-colors">
            <span><icon icon="ic:outline-cloud-download" class="-mb-1 mr-2 text-lg" /></span> <span>Download</span>
          </a>
        </div>
      </div>
    </div>

    <div class="titlemanager-site-content">
      <slot />
    </div>

    <div class="footer" />
  </div>
</template>

<style>
.titlemanager-site {
  display: flex;
  min-height: 100vh;
  flex-direction: column;
}

.titlemanager-site-content {
  flex: 1;
}
</style>

<script lang="ts" setup>
import TmImage from '~/components/TmImage.vue'
import logoImage from '~/assets/img/logo.png'
import spigotImage from '~/assets/img/spigot.png'
import { Icon } from '@iconify/vue'
import TmMinecraftText from "~/components/TmMinecraftText.vue";
import {computed, ref} from "@vue/reactivity";

const logoText = ref('TitleManager')
const isServer = computed(() => process.server)
</script>

<script lang="ts">
export default {
  mounted () {
    const test = this.$titlemanager.shine('TitleManager', '&3&l', '&b&l', 100, true, (it) => {
      this.logoText = it
    })

    setTimeout(() => {
      console.log('stopping', test)
      test()
    }, 5000)
  }
}
</script>
