<template>
  <tm-configuration-section title="Player list">
    <template #header>
      <div>
        Sets the title that is sent to the player when they join the server.
      </div>
    </template>

    <template #fields>
      <div class="font-serif font-bold">enabled</div>
      <div>Toggles this feature.</div>
      <tm-switch v-model="editableConfig.enabled" class="mt-2" />

      <tm-configuration-section-field-seperator />

      <div class="font-serif font-bold">header</div>
      <div>The lines of the scoreboard (Maximum of 15 lines allowed).</div>
      <tm-list-editor v-model="editableConfig.header" />

      <tm-configuration-section-field-seperator />

      <div class="font-serif font-bold">footer</div>
      <div>A list of worlds that the scoreboard should not be shown in.</div>
      <tm-list-editor v-model="editableConfig.footer" />
    </template>

    <template #right>
      <tm-minecraft-player-list :header="editableConfig.header.join('\n')" :footer="editableConfig.footer.join('\n')" />

      <tm-code-block v-model="configOutput" language="yml" />
    </template>
  </tm-configuration-section>
</template>

<script lang="ts" setup>
import {PropType, reactive, ref, watch} from '@vue/runtime-core'
import { cloneDeep } from 'lodash-es'
import TmConfigurationSection from '~/components/docs/configuration/TmConfigurationSection.vue'
import TmConfigurationSectionFieldSeperator from '~/components/docs/configuration/TmConfigurationSectionFieldSeperator.vue'
import TmMinecraftPlayerList from "~/components/TmMinecraftPlayerList.vue";
import TmSwitch from "~/components/elements/TmSwitch.vue";
import TmCodeBlock from "~/components/elements/TmCodeBlock.vue";
import TmListEditor from "~/components/elements/TmListEditor.vue";
import yaml from "js-yaml";

interface PlayerList {
  enabled: boolean
  title: string
  lines: string[]
  'disabled-worlds': string[]
}

interface OutputSchema {
  'player-list': PlayerList
}

const props = defineProps({
  config: {
    type: Object as PropType<PlayerList>,
    required: true
  }
})

const emits = defineEmits<{
  (eventName: 'changed', output: string)
}>()

const editableConfig = reactive(cloneDeep(props.config))

const createOutput = () => {
  const output: OutputSchema = {
    'player-list': editableConfig
  }

  return yaml.dump(output)
}

const configOutput = ref(createOutput())

watch(editableConfig, () => {
  const dump = createOutput()
  configOutput.value = dump
  emits('changed', dump)
})
</script>
