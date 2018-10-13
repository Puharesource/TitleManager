const shine = (text, index) => {
    const getShinePattern = () => /\[(.+);(.+)](.+)/g

    const getShineTextLength = (text) => {
        const match = getShinePattern().exec(text)

        if (match) {
            return match[3].length
        }

        return text.length
    }

    let fadeIn = 0
    let stay = 5
    let fadeOut = 0

    let mainColor = '§6'
    let secondaryColor = '§e'

    // Check if the text has timings in the front.
    if (hasTimings(text)) {
        let timings = getTimings(text)

        text = timings[0]
        fadeIn = timings[1]
        stay = timings[2]
        fadeOut = timings[3]

        // Check if the text still has timings. (This will be used only for the first frame)
        if (hasTimings(text)) {
            timings = getTimings(text)
            text = timings[0]

            // If this is the first frame set the timings.
            if (index === 0) {
                fadeIn = timings[1]
                stay = timings[2]
                fadeOut = timings[3]
            }

            // Check if the text still has timings. (This will be used only for the last frame)
            if (hasTimings(text)) {
                timings = getTimings(text)
                text = timings[0]

                if (index >= getShineTextLength(text) + 3) {
                    fadeIn = timings[1]
                    stay = timings[2]
                    fadeOut = timings[3]
                }
            }
        }
    }

    const match = getShinePattern().exec(text)

    if (match) {
        mainColor = match[1]
        secondaryColor = match[2]
        text = match[3]
    }

    const length = text.length
    let manipulated = mainColor + text

    if (index !== 0 && index < length + 3) {
        let startIndex = index - 3

        if (startIndex < 0) {
            startIndex = 0
        }

        manipulated = mainColor + text.slice(0, startIndex) + secondaryColor + text.slice(startIndex, index) + mainColor + text.slice(index)
    }

    return tmResult(manipulated, index >= length + 3, fadeIn, stay, fadeOut)
}