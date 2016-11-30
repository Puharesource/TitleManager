var mcColors = {};
var mcFormats = {};

var mcColorsBackwards = {};
var mcFormatsBackwards = {};

var addColor = function(char) {
    mcColors["§" + char] = "mc-format-" + char;
    mcColorsBackwards["mc-format-" + char] = "§" + char
};

var addFormat = function (char) {
    mcFormats["§" + char] = "mc-format-" + char;
    mcFormatsBackwards["mc-format-" + char] = "§" + char;
};

addColor("a");
addColor("b");
addColor("c");
addColor("d");
addColor("e");
addColor("f");
addColor("0");
addColor("1");
addColor("2");
addColor("3");
addColor("4");
addColor("5");
addColor("6");
addColor("7");
addColor("8");
addColor("9");

addFormat("k");
addFormat("l");
addFormat("m");
addFormat("n");
addFormat("o");
addFormat("r");

var createColoredSpan = function(text, color, format) {
    var item = document.createElement('span');
    item.classList.add('mc-format');

    if (color != null) {
        item.classList.add(color);
    }

    if (format.length != 0) {
        for (var i = 0; format.length > i; i++) {
            item.classList.add(format[i]);
        }
    }

    item.innerText = text;
    return item;
};

var getFormattedText = function(text) {
    return getFormattedTextAt(text, 0, text.length);
};

var getFormattedTextAt = function(text, from, to) {
    var itemList = [];
    var currentColor = null;
    var currentFormats = [];
    var currentText = "";
    var item;
    var split = !(from === 0 && to === text.length);
    var realIndex = 0;
    var x;

    for (var i = 0; text.length > i; i++) {
        var c = text.charAt(i);

        if (c === '\\' && i + 1 !== text.length && text.charAt(i + 1) === 'n') {
            if (currentText !== '') {
                itemList.push(createColoredSpan(currentText, currentColor, currentFormats));
                currentColor = null;
                currentFormats = [];
                currentText = "";
            }

            item = document.createElement('br');
            itemList.push(item);
            itemList.push(createColoredSpan('', null, 'mc-format-r'));
            i++;
            realIndex--;
        } else if (c === '§' && i + 1 !== text.length) {
            var color = mcColors[c+ text.charAt(i+ 1)];
            var format = mcFormats[c + text.charAt(i+ 1)];

            if ( color != null || format != null) {
                i++;
                realIndex--;
            }

            if((color == null && format == null) && (!split || (realIndex >= from && realIndex <= to))) {
                currentText+=c;
            } else if(color != null || format === 'mc-format-r') {
                itemList.push(createColoredSpan(currentText, currentColor, currentFormats));
                if(format === 'mc-format-r') {
                    itemList.push(createColoredSpan('', null, 'mc-format-r'));
                }

                currentColor = color;
                currentFormats = [];
                currentText = "";
            } else if(format != null) {
                itemList.push(createColoredSpan(currentText, currentColor, currentFormats));
                currentFormats.push(format);
                currentText="";
            }
        } else if (!split || (realIndex >= from && realIndex < to)){
            currentText += c;
        }

        realIndex++;
        x = i;
    }

    if (currentText !== "") {
        itemList.push(createColoredSpan(currentText, currentColor, currentFormats));
    }

    return itemList;
};

var getPlainText = function(itemList) {
    var text = "";

    for(var i = 0; itemList.length > i; i++) {
        var item = itemList[i], classList = item.classList, formats = [], color = null;

        for(var j = 0; classList.length > j; j++) {
            var clazz = classList[j];
            color = mcColorsBackwards[clazz];
            var format = mcFormatsBackwards[clazz];

            if(format!=null) {
                formats.push(format);
            }
        }

        if (color != null) {
            text += color;
        }

        for (var k = 0; formats.length > k; k++) {
            text += formats[k];
        }

        text += item.innerText;
    }

    return text;
};

var getStrippedText = function(text) {
    for (var color in mcColors) {
        if (!mcColors.hasOwnProperty(color)) continue;

        text = text.split(color).join('');
    }

    for (var format in mcFormats) {
        if (!mcFormats.hasOwnProperty(format)) continue;

        text = text.split(format).join('');
    }

    return text;
};