/*!
 * TitleManager Generator By Tarkan Nielsen.
 * I am by no means a JavaScripts developer, I just made this script to help others.
 */

var TypeEnum = {
    TYPE_LETTER: {},
    TYPE_WORD: {},
    COLOR_CHANGING: {}
};

var type = TypeEnum.TYPE_LETTER;

function setType(type) {
    this.type = type;
    switch (type) {
        case TypeEnum.TYPE_LETTER:
            document.getElementById("type-button-type-letter").classList.add("active");
            document.getElementById("type-button-type-word").classList.remove("active");
            document.getElementById("type-button-color-changing").classList.remove("active");
            document.getElementById("div-color-changing-animation").hidden = true;
            break;
        case TypeEnum.TYPE_WORD:
            document.getElementById("type-button-type-letter").classList.remove("active");
            document.getElementById("type-button-type-word").classList.add("active");
            document.getElementById("type-button-color-changing").classList.remove("active");
            document.getElementById("div-color-changing-animation").hidden = true;
            break;
        case TypeEnum.COLOR_CHANGING:
            document.getElementById("type-button-type-letter").classList.remove("active");
            document.getElementById("type-button-type-word").classList.remove("active");
            document.getElementById("type-button-color-changing").classList.add("active");
            document.getElementById("div-color-changing-animation").hidden = false;
            break;
        default:
            errorPopup();
            break;
    }
}

function generate() {
    document.getElementById("textarea-animation").value = "";

    if (isConfigOutput()) {
        var title = document.getElementById("config-name").value;

        if (title.length == 0)
            addLine("'My-Generated-Animation':");
        else addLine("'" + document.getElementById("config-name").value + "':");
        addLine("  frames:")
    }

    if (type == TypeEnum.TYPE_LETTER || null) {
        typeLetter();
    } else if (type == TypeEnum.TYPE_WORD) {
        typeWord();
    } else if (type == TypeEnum.COLOR_CHANGING) {
        colorChanging();
    } else {
        errorPopup();
    }
}

function addLine(line) {
    document.getElementById("textarea-animation").value += line + "\n"
}

function errorPopup() {
    document.getElementById("alert-box").hidden = false;
}

function changeOutputStyle() {
    document.getElementById("div-titlemanager-output-settings").hidden = !isConfigOutput();
}

function isConfigOutput() {
    return document.getElementById("checkbox-output-style").checked;
}

function typeLetter() {
    var chars = document.getElementById("textbox-full-text").value.split("");

    for (var i = 0; chars.length > i; i++) {
        var text = "";
        for (var j = 0; i >= j; j++)
            text += chars[j];
        if (text.substring(text.length -1, text.length) == "&") continue;

        if (i == 0 && i == chars.length - 1) {
            addLine(isConfigOutput() ? "  - '[" + document.getElementById("config-fade-in").value + ";" + document.getElementById("config-stay").value + ";" + document.getElementById("config-fade-in").value + "]" + text + "'" : text);
        } else if (i == 0) {
            addLine(isConfigOutput() ? "  - '[" + document.getElementById("config-fade-in").value + ";" + document.getElementById("config-stay").value + ";0]" + text + "'" : text);
        } else if (i == chars.length - 1) {
            addLine(isConfigOutput() ? "  - '[0;" + document.getElementById("config-stay").value + ";" + document.getElementById("config-fade-out").value + "]" + text + "'" : text);
        } else addLine(isConfigOutput() ? "  - '[0;" + document.getElementById("config-stay").value + ";0]" + text + "'" : text);
    }
}

function typeWord() {
    var words = document.getElementById("textbox-full-text").value.split(" ");

    for (var i = 0; words.length > i; i++) {
        var text = "";
        for (var j = 0; i >= j; j++)
            text += i == j ? words[j] : words[j] + " ";
        if (i == 0 && i == words.length - 1) {
            addLine(isConfigOutput() ? "  - '[" + document.getElementById("config-fade-in").value + ";" + document.getElementById("config-stay").value + ";" + document.getElementById("config-fade-in").value + "]" + text + "'" : text);
        } else if (i == 0) {
            addLine(isConfigOutput() ? "  - '[" + document.getElementById("config-fade-in").value + ";" + document.getElementById("config-stay").value + ";0]" + text + "'" : text);
        } else if (i == words.length - 1) {
            addLine(isConfigOutput() ? "  - '[0;" + document.getElementById("config-stay").value + ";" + document.getElementById("config-fade-out").value + "]" + text + "'" : text);
        } else addLine(isConfigOutput() ? "  - '[0;" + document.getElementById("config-stay").value + ";0]" + text + "'" : text);
    }
}

function colorChanging() {
    var text = document.getElementById("textbox-full-text").value;
    var colors = document.getElementById("animation-colors").value.split(",");

    for (var i = 0; colors.length > i; i++) {
        if (i == 0 && i == colors.length - 1) {
            addLine(isConfigOutput() ? "  - '[" + document.getElementById("config-fade-in").value + ";" + document.getElementById("config-stay").value + ";" + document.getElementById("config-fade-in").value + "]" + colors[i] + text + "'" : colors[i] + text);
        } else if (i == 0) {
            addLine(isConfigOutput() ? "  - '[" + document.getElementById("config-fade-in").value + ";" + document.getElementById("config-stay").value + ";0]" + colors[i] + text + "'" : colors[i] + text);
        } else if (i == colors.length - 1) {
            addLine(isConfigOutput() ? "  - '[0;" + document.getElementById("config-stay").value + ";" + document.getElementById("config-fade-out").value + "]" + colors[i] + text + "'" : colors[i] + text);
        } else addLine(isConfigOutput() ? "  - '[0;" + document.getElementById("config-stay").value + ";0]" + colors[i] + text + "'" : colors[i] + text);
    }
}