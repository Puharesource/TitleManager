<template>
  <span class="mc-font" v-html="computedHtml" />
</template>

<script lang="ts" setup>
import { computed } from '@vue/reactivity'
import McService from '~/services/McService'

const props = defineProps({
  text: {
    type: String,
    required: true
  }
})

const computedHtml = computed(() => {
  const { text } = props

  return McService.createFormattedElements(text).map(it => it.outerHTML).join('')
})
</script>
