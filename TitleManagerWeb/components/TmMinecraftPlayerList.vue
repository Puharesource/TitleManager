<template>
  <div class="mc-font text-white">
    <div class="bg-[#00000055] text-center mb-4 overflow-x-scroll whitespace-nowrap">
      <tm-minecraft-text :text="header" />
    </div>

    <div class="bg-[#00000055] text-center overflow-x-scroll whitespace-nowrap">
      <tm-minecraft-text :text="footer" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import TmMinecraftText from '~/components/TmMinecraftText.vue'

const props = defineProps({
  header: {
    type: String,
    required: true
  },
  footer: {
    type: String,
    required: true
  },
})
</script>
