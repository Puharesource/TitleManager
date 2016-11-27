var getTimingsPattern = function () {
    return /^\[([-]?\d+);([-]?\d+);([-]?\d+)](.+)$/g;
};

var tmResult = function(text, done, fadeIn, stay, fadeOut) {
    return Java.to([text, done, fadeIn, stay, fadeOut])
};

var hasTimings = function (text) {
    return text.match(getTimingsPattern()) != null;
};

var getTimings = function (text) {
    var match = getTimingsPattern().exec(text);

    var fadeIn = parseInt(match[1]);
    var stay = parseInt(match[2]);
    var fadeOut = parseInt(match[3]);
    var groupText = match[4];

    return [groupText, fadeIn, stay, fadeOut]
};

var parseInt = function(string, radix) {
    var val = NaN;

    try {
        if (radix) {
            val = java.lang.Integer.parseInt(string, radix);
        } else {
            val = java.lang.Integer.parseInt(string);
        }
    } catch (e) {}

    return val;
};