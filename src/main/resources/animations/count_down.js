const count_down = (text, index) => {
    let fadeIn = 0
    let stay = 20
    let fadeOut = 0

    if (hasTimings(text)) {
        const timings = getTimings(text)

        text = timings[0]
        fadeIn = timings[1]
        stay = timings[2]
        fadeOut = timings[3]
    }

    const countdown = parseInt(text) - index

    return tmResult(countdown.toString(), countdown <= 1, fadeIn, stay, fadeOut)
}