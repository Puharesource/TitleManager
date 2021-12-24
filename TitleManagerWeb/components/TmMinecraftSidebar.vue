<template>
  <div class="mc-font bg-[#00000055] text-white inline-block">
    <div class="bg-[#00000022] text-center">
      <tm-minecraft-text :text="title" />
    </div>
    <div class="p-2">
      <div v-for="(item, index) in computedItems" :key="index" class="flex leading-tight">
        <div class="flex-grow">
          <tm-minecraft-text :text="item" />
        </div>

        <div class="flex-shrink mc-format-c ml-4">
          {{ 15 - index }}
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { PropType } from '@vue/runtime-core'
import TmMinecraftText from "~/components/TmMinecraftText.vue";
import {computed} from "@vue/reactivity";

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  items: {
    type: Array as PropType<Array<string>>,
    required: true
  }
})

const computedItems = computed(() => {
  if (props.items.length > 15) {
    return props.items.slice(0, 15)
  }

  return props.items
})
</script>
