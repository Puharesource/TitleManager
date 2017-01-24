var tps = function(text, index) {
    var getTPSAt = function (output, index) {
        return /([§][a-f0-9].+), ([§][a-f0-9].+), ([§][a-f0-9].+)/g.exec(output.substring(29))[index];
    };

    var fadeIn = 10;
    var stay = 40;
    var fadeOut = 10;

    if (hasTimings(text)) {
        var timings = getTimings(text);

        text = timings[0];
        fadeIn = timings[1];
        stay = timings[2];
        fadeOut = timings[3];
    }

    var sender = createCommandSender();
    sendCommand(sender, "tps");

    var output = sender.getReceivedMessages().get(0);

    if (text.toUpperCase() === "FULL") {
        text = output;
    } else if (text.toUpperCase() === "SHORT") {
        text = output.substring(29);
    } else if (text === "1") {
        text = getTPSAt(output, 1);
    } else if (text === "5") {
        text = getTPSAt(output, 2);
    } else if (text === "15") {
        text = getTPSAt(output, 3);
    }

    return tmResult(text, true, fadeIn, stay, fadeOut)
};