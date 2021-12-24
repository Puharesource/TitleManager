<template>
  <div class="border-l-6 p-4" :style="{ 'background-color': bgColor, color: textColor, 'border-color': borderColor }">
    <div class="flex">
      <div class="flex-shrink">
        <icon icon="akar-icons:circle-alert" class="text-2xl" style="height: 28px;" />
      </div>

      <div class="flex-grow pl-4">
        <div class="font-bold text-white font-serif text-lg" v-text="props.title" />

        <slot />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { Icon } from '@iconify/vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  }
})

const bgColor = '#744210'
const textColor = '#fbd38d'
const borderColor = '#b7791f'
</script>
