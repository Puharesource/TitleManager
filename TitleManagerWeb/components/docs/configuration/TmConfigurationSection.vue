<template>
  <div class="grid grid-cols-2 gap-16">
    <div>
      <div class="text-2xl font-serif font-bold mb-4">{{ title }}</div>
      <slot name="header" />

      <div class="text-lg font-serif font-bold mt-4">Fields</div>

      <tm-configuration-section-field-seperator />

      <slot name="fields" />
    </div>

    <div class="pt-12">
      <slot name="right" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import TmConfigurationSectionFieldSeperator from '~/components/docs/configuration/TmConfigurationSectionFieldSeperator.vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  }
})
</script>
