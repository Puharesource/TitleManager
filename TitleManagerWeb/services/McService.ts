export interface MinecraftFormat {
  readonly name: string
  readonly code: string
  readonly nativeCode: string
  readonly cssClass: string
  readonly hex?: string
  readonly backgroundHex?: string
  readonly isColor: boolean
  readonly isDark: boolean
  readonly isLight: boolean
}

function createFormat (name: string, code: string, hex?: string, backgroundHex?: string, isDark?: boolean): MinecraftFormat {
  return {
    name,
    code: `&${code}`,
    nativeCode: `§${code}`,
    cssClass: `mc-format-${code}`,
    hex,
    backgroundHex,
    isColor: !!hex,
    isDark: !!isDark,
    isLight: !isDark
  }
}

export class MinecraftFormats {
  static green = createFormat('Green', 'a', '55FF55', '153F15', false)
  static aqua = createFormat('Aqua', 'b', '55FFFF', '153F3F', false)
  static red = createFormat('Red', 'c', 'FF5555', '3F1515', false)
  static lightPurple = createFormat('Light purple', 'd', 'FF55FF', '3F153F', false)
  static yellow = createFormat('Yellow', 'e', 'FFFF55', '3F3F15', true)
  static white = createFormat('White', 'f', 'FFFFFF', '3F3F3F', false)
  static black = createFormat('Black', '0', '000000', '000000', true)
  static darkBlue = createFormat('Dark blue', '1', '0000AA', '00002A', true)
  static darkGreen = createFormat('Dark green', '2', '00AA00', '002A00', true)
  static darkAqua = createFormat('Dark aqua', '3', '00AAAA', '002A2A', true)
  static darkRed = createFormat('Dark red', '4', 'AA0000', '2A0000', true)
  static darkPurple = createFormat('Dark purple', '5', 'AA00AA', '2A002A', true)
  static gold = createFormat('Gold', '6', 'FFAA00', '2A2A00', true)
  static gray = createFormat('Gray', '7', 'AAAAAA', '2A2A2A', false)
  static darkGray = createFormat('Dark gray', '8', '555555', '151515', true)
  static blue = createFormat('Blue', '9', '5555FF', '15153F', true)

  static obfuscated = createFormat('Obfuscated', 'k')
  static bold = createFormat('Bold', 'l')
  static strikethrough = createFormat('Strikethrough', 'm')
  static underline = createFormat('Underline', 'n')
  static italic = createFormat('Italic', 'o')
  static reset = createFormat('Reset', 'r')
}

export default class McService {
  static get colors (): Array<MinecraftFormat> {
    return [
      MinecraftFormats.green,
      MinecraftFormats.aqua,
      MinecraftFormats.red,
      MinecraftFormats.lightPurple,
      MinecraftFormats.yellow,
      MinecraftFormats.white,
      MinecraftFormats.black,
      MinecraftFormats.darkBlue,
      MinecraftFormats.darkGreen,
      MinecraftFormats.darkAqua,
      MinecraftFormats.darkRed,
      MinecraftFormats.darkPurple,
      MinecraftFormats.gold,
      MinecraftFormats.gray,
      MinecraftFormats.darkGray,
      MinecraftFormats.blue
    ]
  }

  static get formats (): Array<MinecraftFormat> {
    return [
      MinecraftFormats.obfuscated,
      MinecraftFormats.bold,
      MinecraftFormats.strikethrough,
      MinecraftFormats.underline,
      MinecraftFormats.italic,
      MinecraftFormats.reset
    ]
  }

  static getFormatFromColorCode (code: string): MinecraftFormat | undefined {
    return this.colors.find(format => format.code === code)
  }

  static getFormatFromFormatCode (code: string): MinecraftFormat | undefined {
    return this.formats.find(format => format.code === code)
  }

  static getFormatFromColorOrFormatCode (code: string): MinecraftFormat | undefined {
    return [...this.colors, ...this.formats].find(format => format.code === code)
  }

  static getFormatFromColorCssClass (cssClass: string): MinecraftFormat | undefined {
    return this.colors.find(format => format.cssClass === cssClass)
  }

  static getFormatFromFormatCssClass (cssClass: string): MinecraftFormat | undefined {
    return this.formats.find(format => format.cssClass === cssClass)
  }

  static getFormatFromColorOrFormatCssClass (cssClass: string): MinecraftFormat | undefined {
    return [...this.colors, ...this.formats].find(format => format.cssClass === cssClass)
  }

  static stripText (text: string): string {
    for (const format of [...this.colors, ...this.formats]) {
      text = text.split(format.code).join('')
    }

    return text
  }

  static getFormatsFromText (text: string): Array<MinecraftFormat> {
    const formats: Array<MinecraftFormat> = []

    for (let i = 1; i < text.length; i++) {
      const code = text.charAt(i - 1) + text.charAt(i)

      const color = this.getFormatFromColorCode(code)

      if (color) {
        formats.push(color)
      } else {
        const format = this.getFormatFromFormatCode(code)

        if (format) {
          formats.push(format)
        }
      }
    }

    return formats
  }

  static getFormatsCssFromText (text: string): Array<string> {
    return this.getFormatsFromText(text).map(format => format.cssClass)
  }

  static createFormattedElement (text: string, colorCss?: string | null, formatCss?: Array<string> | null): HTMLElement {
    const element = document.createElement('span')

    element.textContent = text
    element.classList.add('mc-format')

    if (colorCss) {
      element.classList.add(colorCss)
    }

    if (formatCss) {
      for (const format of formatCss) {
        element.classList.add(format)
      }
    }

    return element
  }

  static createFormattedElements (text: string, from: number = 0, to: number = text.length) {
    const elements: Array<HTMLElement> = []

    let currentColor: string | null | undefined = null
    let currentFormats: Array<string> = []
    let currentText = ''
    const split = from !== 0 || to !== text.length
    let realIndex = 0

    const createSpan = (removeColor: boolean) => {
      elements.push(this.createFormattedElement(currentText, currentColor, currentFormats))

      if (removeColor) {
        currentColor = null
      }

      currentFormats = []
      currentText = ''
    }

    for (let i = 0; i < text.length; i++) {
      const c = text.charAt(i)
      const nextC = i + 1 < text.length ? text.charAt(i + 1) : null

      if ((c === '\\' && nextC === 'n') || c === '\n') {
        if (currentText !== '') {
          createSpan(true)
        }

        elements.push(document.createElement('br'))
        elements.push(this.createFormattedElement('', null, [MinecraftFormats.reset.cssClass]))

        if (nextC === null) {
          elements.push(document.createElement('br'))
        }

        if (c !== '\n') {
          i++
          realIndex--
        }
      } else if (c === '&' && nextC) {
        const color = this.getFormatFromColorCode(c + nextC)?.cssClass
        const format = this.getFormatFromFormatCode(c + nextC)?.cssClass

        if (color || format) {
          i++
          realIndex--
        }

        if (!color && !format && (split || (realIndex >= from && realIndex <= to))) {
          currentText += c
        } else if (color || format === MinecraftFormats.reset.cssClass) {
          createSpan(true)
          elements.push(this.createFormattedElement('', null, [MinecraftFormats.reset.cssClass]))

          currentColor = color
        } else if (format) {
          createSpan(false)
          currentFormats.push(format)
        }
      } else if (!split || (realIndex >= from && realIndex < to)) {
        currentText += c
      }

      realIndex++
    }

    if (currentText.length) {
      elements.push(this.createFormattedElement(currentText, currentColor, currentFormats))
    }

    return elements
  }

  static createFormattedStringFromElements (elements: Array<HTMLElement>): string {
    let text = ''

    for (const item of elements) {
      const classList = item.classList
      const formats: string[] = []
      let color: string | undefined | null = null

      for (const cssClass of Array.from(classList)) {
        const currentColor = McService.getFormatFromColorCssClass(cssClass)?.code
        const currentFormat = McService.getFormatFromFormatCssClass(cssClass)?.code

        if (currentColor) {
          color = currentColor
        }

        if (currentFormat) {
          formats.push(currentFormat)
        }
      }

      if (color) {
        text += color
      }

      for (const format of formats) {
        text += format
      }

      text += item.textContent
    }

    return text
  }
}
