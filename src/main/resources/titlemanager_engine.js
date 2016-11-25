var timingsPattern = /^\[([-]?\d+);([-]?\d+);([-]?\d+)](.+)$/g;

var tmResult = function(text, done, fadeIn, stay, fadeOut) {
    return Java.to([text, done, fadeIn, stay, fadeOut])
};

var hasTimings = function (text) {
    return text.match(timingsPattern) != null;
};

var getTimings = function (text) {
    var match = text.match(timingsPattern);

    var fadeIn = parseInt(match.group(1));
    var stay = parseInt(match.group(2));
    var fadeOut = parseInt(match.group(3));
    var groupText = match.group(4);

    return [groupText, fadeIn, stay, fadeOut]
};