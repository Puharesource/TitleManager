<template>
  <pre v-html="html" class="language-yaml rounded-md overflow-y-scroll monospace" style="max-width: 100%;" />
</template>

<script lang="ts" setup>
import { computed } from '@vue/reactivity'
import { useVModel } from '@vueuse/core'
import { useNuxtApp } from "#app";

const props = defineProps({
  modelValue: {
    type: String,
    required: true
  },
  language: {
    type: String,
    required: true
  }
})

const value = useVModel(props)
const app = useNuxtApp()
const html = computed(() => {
  return app.$prism.highlight(value.value, app.$prism.languages[props.language], props.language)
})
</script>
