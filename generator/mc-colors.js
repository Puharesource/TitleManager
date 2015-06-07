var mcColors, mcFormats, mcColorsBackwards, mcFormatsBackwards, getFormattedText, getFormattedTextAt, createColoredSpan, getPlainText, getStrippedText;

mcColors = {
    // ---- Colors ---- \\
    // a-f
    '&a': 'mc-format-a',
    '&b': 'mc-format-b',
    '&c': 'mc-format-c',
    '&d': 'mc-format-d',
    '&e': 'mc-format-e',
    '&f': 'mc-format-f',
    // 0-9
    '&0': 'mc-format-0',
    '&1': 'mc-format-1',
    '&2': 'mc-format-2',
    '&3': 'mc-format-3',
    '&4': 'mc-format-4',
    '&5': 'mc-format-5',
    '&6': 'mc-format-6',
    '&7': 'mc-format-7',
    '&8': 'mc-format-8',
    '&9': 'mc-format-9'
};

mcFormats = {
    '&k': 'mc-format-k',
    '&l': 'mc-format-l',
    '&m': 'mc-format-m',
    '&n': 'mc-format-n',
    '&o': 'mc-format-o',
    '&r': 'mc-format-r'
};

mcColorsBackwards = {
    // ---- Colors ---- \\
    // a-f
    'mc-format-a': '&a',
    'mc-format-b': '&b',
    'mc-format-c': '&c',
    'mc-format-d': '&d',
    'mc-format-e': '&e',
    'mc-format-f': '&f',
    // 0-9
    'mc-format-0': '&0',
    'mc-format-1': '&1',
    'mc-format-2': '&2',
    'mc-format-3': '&3',
    'mc-format-4': '&4',
    'mc-format-5': '&5',
    'mc-format-6': '&6',
    'mc-format-7': '&7',
    'mc-format-8': '&8',
    'mc-format-9': '&9'
};

mcFormatsBackwards = {
    'mc-format-k': '&k',
    'mc-format-l': '&l',
    'mc-format-m': '&m',
    'mc-format-n': '&n',
    'mc-format-o': '&o',
    'mc-format-r': '&r'
};

createColoredSpan = function(text, color, format) {
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

getFormattedText = function(text) {
    return getFormattedTextAt(text, 0, text.length);
};

getFormattedTextAt = function(text, from, to) {
    var itemList = [], currentColor = null, currentFormats = [], currentText = "", item, split = !(from === 0 && to === text.length), realIndex = 0;

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
        } else if (c === '&' && i + 1 !== text.length) {
            var color = mcColors[c + text.charAt(i + 1)];
            var format = mcFormats[c + text.charAt(i + 1)];

            if (color != null || format != null) {
                i++;
                realIndex--;
            }

            if ((color == null && format == null) && (!split || (realIndex >= from && realIndex <= to))) {
                currentText += c;
            } else if (color != null || format === 'mc-format-r') {
                itemList.push(createColoredSpan(currentText, currentColor, currentFormats));
                if (format === 'mc-format-r') {
                    itemList.push(createColoredSpan('', null, 'mc-format-r'));
                }
                currentColor = color;
                currentFormats = [];
                currentText = "";
            } else if (format != null) {
                itemList.push(createColoredSpan(currentText, currentColor, currentFormats));
                currentFormats.push(format);
                currentText = "";
            }
        } else {
            if (!split || (realIndex >= from && realIndex < to)) {
                currentText += c;
            }
        }

        realIndex++;
        x = i;
    }

    if (currentText !== "") {
        itemList.push(createColoredSpan(currentText, currentColor, currentFormats));
    }

    return itemList;
};

getPlainText = function(itemList) {
    var text = "";

    for (var i = 0; itemList.length > i; i++) {
        var item = itemList[i], classList = item.classList, formats = [], color = null;

        for (var j = 0; classList.length > j; j++) {
            var clazz = classList[j];

            color = mcColorsBackwards[clazz];
            var format = mcFormatsBackwards[clazz];

            if (format != null) {
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

getStrippedText = function(text) {
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