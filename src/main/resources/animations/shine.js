var shine = function(text, index) {
    var getShinePattern = function () {
        return /\[(.+);(.+)](.+)/g;
    };

    var getShineTextLength = function(text) {
        var match = getShinePattern().exec(text);

        if (match !== null) {
            return match[3].length
        }

        return text.length
    };

    var fadeIn = 0;
    var stay = 5;
    var fadeOut = 0;

    var mainColor = '§6';
    var secondaryColor = '§e';

    // Check if the text has timings in the front.
    if (hasTimings(text)) {
        var timings = getTimings(text);

        text = timings[0];
        fadeIn = timings[1];
        stay = timings[2];
        fadeOut = timings[3];

        // Check if the text still has timings. (This will be used only for the first frame)
        if (hasTimings(text)) {
            timings = getTimings(text);
            text = timings[0];

            // If this is the first frame set the timings.
            if (index === 0) {
                fadeIn = timings[1];
                stay = timings[2];
                fadeOut = timings[3];
            }

            // Check if the text still has timings. (This will be used only for the last frame)
            if (hasTimings(text)) {
                timings = getTimings(text);
                text = timings[0];

                if (index >= getShineTextLength(text) + 3) {
                    fadeIn = timings[1];
                    stay = timings[2];
                    fadeOut = timings[3];
                }
            }
        }
    }

    var match = getShinePattern().exec(text);

    if (match !== null) {
        mainColor = match[1];
        secondaryColor = match[2];
        text = match[3];
    }

    var length = text.length;
    var manipulated = mainColor + text;

    if (index !== 0 && index < length + 3) {
        var startIndex = index - 3;

        if (startIndex < 0) {
            startIndex = 0;
        }

        manipulated = mainColor + text.slice(0, startIndex) + secondaryColor + text.slice(startIndex, index) + mainColor + text.slice(index);
    }

    return tmResult(manipulated, index >= length + 3, fadeIn, stay, fadeOut)
};