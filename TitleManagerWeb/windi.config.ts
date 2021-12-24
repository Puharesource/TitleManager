import { defineConfig } from 'windicss/helpers'
import defaultTheme from 'windicss/defaultTheme'

export default defineConfig({
  theme: {
    extend: {
      container: {
        center: true,
        screens: {
          sm: '100%',
          md: '100%',
          lg: '1048px',
          xl: '1280px',
          '2xl': '1280px'
        }
      },

      fontFamily: {
        sans: ['Poppins', ...defaultTheme.fontFamily.sans].join(','),
        serif: ['Quicksand', ...defaultTheme.fontFamily.serif].join(',')
      }
    }
  }
})
