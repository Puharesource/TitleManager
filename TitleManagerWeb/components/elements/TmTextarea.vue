<template>
  <textarea v-model="value" class="w-full p-2 border-[1px] rounded-md resize-none" :rows="height" />
</template>

<script lang="ts" setup>
import { useVModel } from '@vueuse/core'
import { computed } from '@vue/reactivity'

const props = defineProps({
  modelValue: {
    type: String,
    required: true
  },
  minHeight: {
    type: Number,
    default: 1
  },
  maxHeight: {
    type: Number,
    default: null
  }
})

const value = useVModel(props)
const height = computed(() => {
  let height = (value.value as string).split('\n').length

  if (height < props.minHeight) {
    height = props.minHeight
  } else if (props.maxHeight !== null && height > props.maxHeight) {
    height = props.maxHeight
  }

  return height
})
</script>
