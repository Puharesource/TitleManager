<template>
  <tm-textarea v-model="listAsString" :min-height="minHeight" :max-height="maxItems" />
</template>

<script lang="ts" setup>
import { defineEmits, PropType, ref, watch } from "@vue/runtime-core";
import TmTextarea from '~/components/elements/TmTextarea.vue'

const props = defineProps({
  modelValue: {
    type: Array as PropType<Array<string>>,
    required: true
  },
  minHeight: {
    type: Number,
    default: 1
  },
  maxItems: {
    type: Number,
    default: null
  }
})

const emits = defineEmits<{
  (eventName: 'update:modelValue', output: string[])
}>()

const listAsString = ref(props.modelValue.join('\n'))
watch(listAsString, (value) => {
  let list = value.split('\n')

  if (props.maxItems && props.maxItems > list.length) {
    list = list.slice(0, props.maxItems)
  }

  emits('update:modelValue', list)
})
</script>
