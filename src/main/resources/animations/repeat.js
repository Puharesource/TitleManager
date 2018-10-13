const repeat = (text, index) => {
    const getRepeatPattern = () => /\[(\d+)](.+)/g

    let totalStay = 60
    const match = getRepeatPattern().exec(text)

    if (match) {
        totalStay = parseInt(match[1])
        text = match[2]
    }

    let stay = 20
    const indexStay = index * 20

    if (totalStay - indexStay <= 0) {
        return tmResult(" ", true, 0, 1, 0)
    } else if (totalStay - indexStay - 20 < 20) {
        stay = totalStay - indexStay - 20
    }

    return tmResult(text, false, 0, stay, 0)
}