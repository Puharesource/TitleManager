var text_write = function(text, index) {
    var fadeIn = 0;
    var stay = 5;
    var fadeOut = 0;

    if (hasTimings(text)) {
        var timings = getTimings(text);

        text = timings[0];
        fadeIn = timings[1];
        stay = timings[2];
        fadeOut = timings[3];
    }

    var manipulated = text.substring(index, text.length);

    return tmResult(manipulated, text.length == index, fadeIn, stay, fadeOut)
};