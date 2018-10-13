const marquee = (text, index) => {
    const getMarqueePattern = () => /\[(\d+)](.+)/g

    let width = 0;

    let fadeIn = 0;
    let stay = 2;
    let fadeOut = 0;

    if (hasTimings(text)) {
        const timings = getTimings(text);

        text = timings[0];
        fadeIn = timings[1];
        stay = timings[2];
        fadeOut = timings[3];
    }

    const match = getMarqueePattern().exec(text);

    if (match) {
        width = parseInt(match[1]);
        text = match[2];
    }

    if (width <= 0 || text.length < width) {
        width = text.length;
    }

    let marquee = '';

    for (let i = 0; i < width; i++) {
        marquee += text.charAt((index + i) % text.length);
    }

    return tmResult(marquee, text.length <= index, fadeIn, stay, fadeOut)
}