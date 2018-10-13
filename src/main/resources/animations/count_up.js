const count_up = (text, index) => {
    index++

    let fadeIn = 0
    let stay = 20
    let fadeOut = 0

    if (hasTimings(text)) {
        let timings = getTimings(text)

        text = timings[0]
        fadeIn = timings[1]
        stay = timings[2]
        fadeOut = timings[3]
    }

    const limit = parseInt(text)

    return tmResult(index.toString(), index >= limit, fadeIn, stay, fadeOut)
}