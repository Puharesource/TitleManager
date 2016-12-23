var marquee = function(text, index) {
    var getMarqueePattern = function () {
        return /\[(\d+)](.+)/g;
    };

    var width = 0;

    var fadeIn = 0;
    var stay = 2;
    var fadeOut = 0;

    if (hasTimings(text)) {
        var timings = getTimings(text);

        text = timings[0];
        fadeIn = timings[1];
        stay = timings[2];
        fadeOut = timings[3];
    }

    var match = getMarqueePattern().exec(text);

    if (match != null) {
        width = parseInt(match[1]);
        text = match[2];
    }

    if (width <= 0 || text.length < width) {
        width = text.length;
    }

    var marquee = '';

    for (var i = 0; i < width; i++) {
        marquee += text.charAt((index + i) % text.length);
    }

    return tmResult(marquee, text.length <= index, fadeIn, stay, fadeOut)
};