<template>
  <tm-configuration-section title="Scoreboard">
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

      <div class="font-serif font-bold">title</div>
      <div>The title displayed at the very top of the scoreboard.</div>
      <div><span class="font-bold">WARNING! (1.12 or below ONLY)</span> The title must consist of 32 or less characters (this includes color codes)</div>
      <input v-model="editableConfig.title" type="text" class="w-full p-2 border-[1px] rounded-md">

      <tm-configuration-section-field-seperator />

      <div class="font-serif font-bold">lines</div>
      <div>The lines of the scoreboard (Maximum of 15 lines allowed).</div>
      <tm-list-editor v-model="editableConfig.lines" :max-items="15" />

      <tm-configuration-section-field-seperator />

      <div class="font-serif font-bold">disabled-worlds</div>
      <div>A list of worlds that the scoreboard should not be shown in.</div>
      <tm-list-editor v-model="editableConfig['disabled-worlds']" />
    </template>

    <template #right>
      <tm-minecraft-sidebar :title="editableConfig.title" :items="editableConfig.lines" />

      <tm-code-block v-model="configOutput" language="yml" />
    </template>
  </tm-configuration-section>
</template>

<script lang="ts" setup>
import { PropType, reactive, ref, watch } from '@vue/runtime-core'
import { cloneDeep } from 'lodash-es'
import TmConfigurationSection from '~/components/docs/configuration/TmConfigurationSection.vue'
import TmConfigurationSectionFieldSeperator from '~/components/docs/configuration/TmConfigurationSectionFieldSeperator.vue'
import TmMinecraftSidebar from '~/components/TmMinecraftSidebar.vue'
import TmSwitch from '~/components/elements/TmSwitch.vue'
import TmCodeBlock from '~/components/elements/TmCodeBlock.vue'
import yaml from "js-yaml";
import TmListEditor from "~/components/elements/TmListEditor.vue";

interface Scoreboard {
  enabled: boolean
  title: string
  lines: string[]
  'disabled-worlds': string[]
}

interface OutputSchema {
  scoreboard: Scoreboard
}

const props = defineProps({
  config: {
    type: Object as PropType<Scoreboard>,
    required: true
  }
})

const emits = defineEmits<{
  (eventName: 'changed', output: string)
}>()

const editableConfig: Scoreboard = reactive(cloneDeep(props.config))

const createOutput = () => {
  const output: OutputSchema = {
    scoreboard: editableConfig
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
