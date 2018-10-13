try {
    load('nashorn:mozilla_compat.js');

    importPackage('org.bukkit');
} catch (e) {}

var Bukkit = Java.type('org.bukkit.Bukkit');
var ChatColor = Java.type('org.bukkit.ChatColor');

var createCommandSender = function() {
    return ScriptCommandSender.newInstance();
};

var isTesting = function () {
    return Bukkit.getServer() === null;
};

var getTimingsPattern = function () {
    return /^\[([-]?\d+);([-]?\d+);([-]?\d+)](.+)$/g;
};

var tmResult = function(text, done, fadeIn, stay, fadeOut) {
    return Java.to([text, done, fadeIn, stay, fadeOut])
};

var hasTimings = function (text) {
    return text.match(getTimingsPattern()) !== null;
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

var sendCommand = function(commandSender, commandLine) {
    if (isTesting()) {
        print("Sending command with command line: " + commandLine);
    } else {
        Bukkit.dispatchCommand(commandSender, commandLine);
    }
};