<template>
  <div class="container">
    <div class="grid grid-cols-4 gap-x-12">
      <div class="col-span-1">
        <div class="text-lg serif font-medium">Animation Types</div>

        <hr class="my-4" />

        <div class="flex flex-col gap-y-2 font-medium">
          <a href="#shine" class="underline-item" :class="{ active: route.hash === '#shine' }">Shine</a>
          <a href="#marquee" class="underline-item" :class="{ active: route.hash === '#marquee' }">Marquee</a>
          <a href="#typetext" class="underline-item" :class="{ active: route.hash === '#typetext' }">Type text</a>
          <a href="#deletetext" class="underline-item" :class="{ active: route.hash === '#deletetext' }">Delete text</a>
          <a href="#count" class="underline-item" :class="{ active: route.hash === '#count' }">Count</a>
          <a href="#countdown" class="underline-item" :class="{ active: route.hash === '#countdown' }">Countdown</a>
        </div>
      </div>

      <div class="col-span-3">
        <div class="text-lg serif font-medium">Generator</div>

        <hr class="my-4" />

        <label for="animation-input" class="text-xs font-medium">Animation input</label>
        <input id="animation-input" v-model="input" type="text" class="p-2 border-[1px] mc-font w-full mb-4" placeholder="Aa">

        <div v-if="route.hash === '#shine'" class="flex gap-4">
          <div>
            <label for="shine-fade-in" class="text-xs font-medium">Fade in</label>
            <input id="shine-fade-in" v-model="shineValues.fadeIn" type="number" min="1" class="p-2 border-[1px] w-full">
          </div>

          <div>
            <label for="shine-speed" class="text-xs font-medium">Speed</label>
            <input id="shine-speed" v-model="shineValues.speed" type="number" min="1" class="p-2 border-[1px] w-full">
          </div>

          <div>
            <label for="shine-fade-out" class="text-xs font-medium">Fade in</label>
            <input id="shine-fade-out" v-model="shineValues.fadeOut" type="number" min="1" class="p-2 border-[1px] w-full">
          </div>

          <div>
            <label for="shine-width" class="text-xs font-medium">Width</label>
            <input id="shine-width" v-model="shineValues.width" type="number" min="1" class="p-2 border-[1px] w-full">
          </div>

          <div>
            <label for="shine-primary-color" class="text-xs font-medium">Primary color</label>
            <input id="shine-primary-color" v-model="shineValues.primaryColor" type="text" class="p-2 border-[1px] w-full" :class="shinePrimaryColorClasses">
          </div>

          <div>
            <label for="shine-secondary-color" class="text-xs font-medium">Secondary color</label>
            <input id="shine-secondary-color" v-model="shineValues.secondaryColor" type="text" class="p-2 border-[1px] w-full" :class="shineSecondaryColorClasses">
          </div>
        </div>

        <div v-else-if="route.hash === '#marquee'" class="flex gap-4">
          <div>
            <label for="marquee-speed" class="text-xs font-medium">Speed</label>
            <input id="marquee-speed" v-model="marqueeValues.speed" type="number" min="1" class="p-2 border-[1px] w-full">
          </div>

          <div>
            <label for="marquee-width" class="text-xs font-medium">Width</label>
            <input id="marquee-width" v-model="marqueeValues.width" type="number" min="-1" class="p-2 border-[1px] w-full">
          </div>
        </div>

        <div v-else-if="route.hash === '#typetext'" class="flex gap-4">
          <div>
            <label for="typetext-speed" class="text-xs font-medium">Speed</label>
            <input id="typetext-speed" v-model="textTypeValues.speed" type="number" min="1" class="p-2 border-[1px] w-full">
          </div>
        </div>

        <div v-else-if="route.hash === '#deletetext'" class="flex gap-4">
          <div>
            <label for="deletetext-speed" class="text-xs font-medium">Speed</label>
            <input id="deletetext-speed" v-model="textDeleteValues.speed" type="number" min="1" class="p-2 border-[1px] w-full">
          </div>
        </div>

        <div v-else-if="route.hash === '#count'" class="flex gap-4">
          <div>
            <label for="count-speed" class="text-xs font-medium">Speed</label>
            <input id="count-speed" v-model="countValues.speed" type="number" min="1" class="p-2 border-[1px] w-full">
          </div>
        </div>

        <div v-else-if="route.hash === '#countdown'" class="flex gap-4">
          <div>
            <label for="countdown-speed" class="text-xs font-medium">Speed</label>
            <input id="countdown-speed" v-model="countdownValues.speed" type="number" min="1" class="p-2 border-[1px] w-full">
          </div>
        </div>

        <div class="mt-4">
          <div class="border-[1px] min-h-48 flex items-center justify-center text-xl relative transition-colors" :style="{ 'background-color': isPreviewDark ? 'black' : 'white', color: isPreviewDark ? 'white' : 'black' }">
            <div class="absolute top-2 right-2 cursor-pointer" @click="isPreviewDark = !isPreviewDark"><icon :icon="isPreviewDark ? 'carbon:moon' : 'carbon:sun'" /></div>
            <tm-minecraft-text v-if="isClient" :text="input" class="whitespace-pre" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useRoute } from '#imports'
import { ref, reactive, computed } from "@vue/reactivity";
import TmMinecraftText from "~/components/TmMinecraftText.vue";
import { Icon } from '@iconify/vue'
import {watch} from "@vue/runtime-core";
import McService from "~/services/McService";

const route = useRoute()

const isClient = computed(() => process.client)
const isPreviewDark = ref(false)
const input = ref('')

const shineValues = reactive({
  fadeIn: 10,
  speed: 3,
  fadeOut: 10,
  width: 3,
  primaryColor: '&3',
  secondaryColor: '&b'
})

const marqueeValues = reactive({
  speed: 5,
  width: -1
})

const textTypeValues = reactive({
  speed: 5
})

const textDeleteValues = reactive({
  speed: 5
})

const countValues = reactive({
  speed: 20
})

const countdownValues = reactive({
  speed: 20
})

const shinePrimaryColorClasses = computed(() => {
  const classObj: any = {}
  const colors = McService.getFormatsCssFromText(shineValues.primaryColor)

  for (const color of colors) {
    classObj[color] = true
  }

  return classObj
})

const shineSecondaryColorClasses = computed(() => {
  const classObj: any = {}
  const colors = McService.getFormatsCssFromText(shineValues.secondaryColor)

  for (const color of colors) {
    classObj[color] = true
  }

  return classObj
})

const shineScriptOutput = computed(() => {
  return `\${shine:[${shineValues.fadeIn};${shineValues.speed};${shineValues.fadeOut}][${shineValues.primaryColor};${shineValues.secondaryColor}]${input}`
})

const marqueeScriptOutput = computed(() => {
  let width = ''

  if (marqueeValues.width > 0) {
    width = `[${marqueeValues.width}]`
  }

  return `\${marquee:[0;${marqueeValues.speed};0]${width}${input.value}`
})

const textTypeScriptOutput = computed(() => {
  return `\${text_write:[0;${textTypeValues.speed};0]${input.value}`
})

const textDeleteScriptOutput = computed(() => {
  return `\${text_delete:[0;${textDeleteValues.speed};0]${input.value}`
})

const countScriptOutput = computed(() => {
  return `\${count_up:[0;${countValues.speed};0]${input.value}`
})

const countdownScriptOutput = computed(() => {
  return `\${count_down:[0;${countdownValues.speed};0]${input.value}`
})

const runningAnimation = ref<null | (() => void)>(null)

watch([input, shineValues, marqueeValues, textTypeValues, textDeleteValues, countValues, countdownValues], ([input, shineValues, marqueeValues, textTypeValues, textDeleteValues, countValues, countdownValues]) => {
  if (!input) {
    if (runningAnimation.value) {
      runningAnimation.value()
    }

    return
  }


})
</script>
