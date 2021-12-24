<template>
  <div>
    <div class="container">
      <tm-configuration-section-player-list v-if="config['player-list']" :config="config['player-list']" />

      <tm-configuration-section-scoreboard v-if="config.scoreboard" :config="config.scoreboard" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from '@vue/runtime-core'
import TmConfigurationSectionScoreboard from '~/components/docs/configuration/TmConfigurationSectionScoreboard.vue'
import { useFetch } from '@vueuse/core'
import yaml from 'js-yaml'
import TmConfigurationSectionPlayerList from "~/components/docs/configuration/TmConfigurationSectionPlayerList.vue";

const { data: yamlText } = await useFetch<string>('/config.yml')
const config = reactive(yaml.load(yamlText.value))
</script>
