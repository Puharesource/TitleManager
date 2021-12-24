<template>
  <div class="inline-block">
    <div class="relative inline-block cursor-pointer" @click="value = !value">
      <input v-model="value" type="checkbox" id="toggleB" class="sr-only">
      <div class="block bg-gray-600 w-12 h-6 rounded-full" />
      <div class="dot absolute left-1 top-1 bg-white w-4 h-4 rounded-full transition" />
    </div>

    <label v-if="label" class="ml-2 mb-1 text-gray-700 font-medium" v-text="label" />
  </div>
</template>

<script lang="ts" setup>
import { useVModel } from '@vueuse/core'

const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  big: {
    type: Boolean,
    default: false
  },
  label: {
    type: String,
    default: null
  }
})

const value = useVModel(props)
</script>

<style>
input ~ .dot {
  transition: all 0.3s;
}

input:checked ~ .dot {
  left: calc(100% - 1.25rem);
  @apply bg-blue-400;
}

input:not(:checked) ~ .dot {
  left: 0.25rem;
}
</style>
