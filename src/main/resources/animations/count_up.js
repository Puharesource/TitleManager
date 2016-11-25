var count_up = function(text, index) {
    var fadeIn = 0;
    var stay = 20;
    var fadeOut = 0;

    if (hasTimings(text)) {
        var timings = getTimings(text);

        text = timings[0];
        fadeIn = timings[1];
        stay = timings[2];
        fadeOut = timings[3];
    }

    var limit = parseInt(text);

    return tmResult(index.toString(), (index >= limit), fadeIn, stay, fadeOut)
};