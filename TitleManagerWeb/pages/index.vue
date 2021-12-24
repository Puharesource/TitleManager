<template>
  <div>
    <div class="container mx-auto">
      <div>
        Countdown
        <pre class="mc-font text-2xl font-bold subpixel-antialiased p-4 bg-gray-900 text-white text-center min-h-16" v-text="countdown" />
      </div>

      <div>
        Count
        <pre class="mc-font text-2xl font-bold subpixel-antialiased p-4 bg-gray-900 text-white text-center min-h-16" v-text="count" />
      </div>

      <div>
        Marquee
        <pre class="mc-font text-2xl font-bold subpixel-antialiased p-4 bg-gray-900 text-white text-center min-h-16" v-text="marquee" />
      </div>

      <div>
        Write
        <pre class="mc-font text-2xl font-bold subpixel-antialiased p-4 bg-gray-900 text-white text-center min-h-16" v-text="write" />
      </div>

      <div>
        Delete
        <pre class="mc-font text-2xl font-bold subpixel-antialiased p-4 bg-gray-900 text-white text-center min-h-16" v-text="deleteText" />
      </div>

      <div>
        Shine
        <div class="mc-font text-2xl font-bold subpixel-antialiased p-4 bg-gray-900 text-white text-center min-h-16">
          <tm-minecraft-text :text="shineText" />
        </div>
      </div>

      <tm-minecraft-sidebar :title="scoreboardTitle" :items="scoreboardLines" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive } from "@vue/runtime-core"
import TmMinecraftSidebar from '~/components/TmMinecraftSidebar.vue'
import TmMinecraftText from "~/components/TmMinecraftText.vue";

const countdown = ref('')
const count = ref('')
const marquee = ref('')
const write = ref('')
const deleteText = ref('')
const shineText = ref('')
const scoreboardLines: string[] = reactive([])
const scoreboardTitle = ref('')
const tasks: (() => void)[] = reactive([])
</script>

<script lang="ts">
import yaml from 'js-yaml'

export default {
  async mounted () {
    const yamlText = await fetch('/config.yml').then(it => it.text())
    const test = yaml.load(yamlText) as any

    this.scoreboardTitle = test.scoreboard.title
    this.scoreboardLines = test.scoreboard.lines

    this.tasks.push(this.$titlemanager.countdown(5, true, (n) => {
      this.countdown = n
      console.log('hello?', n, this.countdown)
    }))

    this.tasks.push(this.$titlemanager.count(5, true, (n) => {
      this.count = n
    }))

    this.tasks.push(this.$titlemanager.marquee('Marquee  Generator  :)  ', 200, true, (it) => {
      this.marquee = it
    }))

    this.tasks.push(this.$titlemanager.writeText('Write Text Generator :^)', 200, true, (it) => {
      this.write = it
    }))

    this.tasks.push(this.$titlemanager.deleteText('Delete Text Generator D:', 200, true, (it) => {
      this.deleteText = it
    }))

    this.tasks.push(this.$titlemanager.shine('Time to shine!', '&3', '&b', 100, true, (it) => {
      this.shineText = it
    }))
  },

  beforeUnmount() {
    this.tasks.forEach(it => it())
  }
}
</script>
