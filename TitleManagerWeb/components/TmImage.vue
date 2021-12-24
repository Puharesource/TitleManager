<template>
  <div :style="outerStyle">
    <div :style="responsiveStyle">
      <div :style="style">
        <slot />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref } from '@vue/reactivity'

const props = defineProps({
  src: {
    type: String,
    required: true
  },
  width: {
    type: String,
    default: null
  },
  height: {
    type: String,
    default: null
  },
  minWidth: {
    type: String,
    default: null
  },
  minHeight: {
    type: String,
    default: null
  },
  maxWidth: {
    type: String,
    default: '100%'
  },
  maxHeight: {
    type: String,
    default: '100%'
  },
  size: {
    type: String,
    default: 'contain'
  },
  position: {
    type: String,
    default: 'center'
  }
})
const image: HTMLImageElement | null = ref(null)
let naturalWidth = ref(0)
let naturalHeight = ref(0)
let calculatedAspectRatio = ref(1)

const responsiveStyle = computed(() => {
  return {
    overflow: 'hidden',
    height: 0,
    position: 'relative',
    'padding-top': `calc(${naturalHeight.value} / ${naturalWidth.value} * 100%)`
  }
})

const width = computed(() => props.width ?? `${naturalWidth.value}px`)
const height = computed(() => props.height ?? `${naturalHeight.value}px`)

const outerStyle = computed(() => {
  return {
    'min-width': props.minWidth,
    'min-height': props.minHeight,
    'max-width': props.maxWidth,
    'max-height': props.maxHeight,
    width,
    height
  }
});

(() => ({
  naturalWidth,
  naturalHeight
}))
</script>

<script lang="ts">
export default {
  methods: {
    onLoad () {
      if (this.image && this.image.naturalHeight && this.image.naturalWidth) {
        this.naturalWidth = this.image.naturalWidth
        this.naturalHeight = this.image.naturalHeight
        this.calculatedAspectRatio = this.image.naturalWidth / this.image.naturalHeight
      } else {
        this.calculatedAspectRatio = 1
      }
    }
  },

  computed: {
    style () {
      return {
        position: 'absolute',
        top: '0',
        left: '0',
        width: '100%',
        height: '100%',
        'background-image': `url(${this.src})`,
        'background-repeat': 'no-repeat',
        'background-position': this.position,
        'background-size': this.size
      }
    }
  },

  mounted () {
    this.image = new Image()

    this.image.onload = async () => {
      if (this.image.decode) {
        try {
          await this.image.decode()

          this.onLoad()
        } catch (e) {
          console.warn(
              `Failed to decode image, trying to render anyway\n\n` +
              `src: ${this.src}` +
              (e.message ? `\nOriginal error: ${e.message}` : ''),
              this
          )
        }
      } else {
        this.onLoad()
      }
    }

    this.image.src = this.src
  }
}
</script>
