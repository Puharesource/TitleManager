var createFrame = function(text, fadeIn, stay, fadeOut) {
    return {
        'text': text,
        'fadeIn': fadeIn,
        'stay': stay,
        'fadeOut': fadeOut
    };
};
var getFadeIn = function() {
    var fadeIn = $('#fade-in').val();
    if (isNumber(fadeIn)) {
        return parseInt(fadeIn);
    } else {
        return 0;
    }
};
var getStay = function() {
    var stay = $('#stay').val();
    if (isNumber(stay)) {
        return parseInt(stay);
    } else {
        return 0;
    }
};
var getFadeOut = function() {
    var fadeOut = $('#fade-out').val();
    if (isNumber(fadeOut)) {
        return parseInt(fadeOut);
    } else {
        return 0;
    }
};
var editFrame = function(i) {
    var text, fadeIn, stay, fadeOut;
    text = $('#animationText-' + i).val();
    fadeIn = $('#fade-in-' + i).val();
    stay = $('#stay-' + i).val();
    fadeOut = $('#fade-out-' + i).val();
    if (isNumber(fadeIn)) {
        fadeIn = parseInt(fadeIn);
    } else {
        fadeIn = 0;
    }
    if (isNumber(stay)) {
        stay = parseInt(stay);
    } else {
        stay = 0;
    }
    if (isNumber(fadeOut)) {
        fadeOut = parseInt(fadeOut);
    } else {
        fadeOut = 0;
    }
    lines[i] = createFrame(text, fadeIn, stay, fadeOut);
    addRows();
    $('#animation-frame-modal-' + i).modal('hide')
};
var addLine = function(line) {
    document.getElementById("config-output").value += line + "\n"
};
var generateConfig = function() {
    document.getElementById('config-output').value = '';
    var name = $('#animation-name').val().toLowerCase().split(' ').join('-');
    addLine("'" + name + "':");
    addLine("  frames:");
    if (lines != null) {
        for (var i = 0; lines.length > i; i++) {
            var frame = lines[i];
            addLine("  - '[" + frame.fadeIn + ';' + frame.stay + ';' + frame.fadeOut + "]" + frame.text.split("'").join("''") + "'")
        }
    }
    $('#config-output').attr('rows', 3 + lines.length)
};
var errorPopup = function(text) {
    var alert = $("#alert-box");
    alert.text(text);
    alert.show();
};
var marqueeGenerator = function(text, width) {
    var lines = [];
    var strippedText = getStrippedText(getPlainText(getFormattedText(text)));
    var len = strippedText.length;
    for (var i = 0; len > i; i++) {
        var newText = "";
        var j = i % len;
        var k = (i + width) % len;
        if (j > k) {
            newText = getPlainText(getFormattedTextAt(text, j, len));
            newText += getPlainText(getFormattedTextAt(text, 0, width - getStrippedText(newText).length));
        } else {
            newText = getPlainText(getFormattedTextAt(text, j, k));
        }
        if (newText.length > 0) {
            lines.push(createFrame(newText, getFadeIn(), getStay(), getFadeOut()));
        }
    }
    return lines;
};
var textTypeGenerator = function(text, width) {
    var lines = [],
        len = getStrippedText(getPlainText(getFormattedText(text))).length;
    for (var i = 0; Math.ceil(len / width) >= i; i++) {
        var end = i * width;
        var newText = getPlainText(getFormattedTextAt(text, 0, end > len ? len : end));
        if (getStrippedText(newText).length > 0) {
            lines.push(createFrame(newText, getFadeIn(), getStay(), getFadeOut()));
        }
    }
    return lines;
};
var wordTypeGenerator = function(text) {
    var lines = [],
        words = text.split(' '),
        currentLength = 0;
    for (var i = 0; words.length > i; i++) {
        var word = words[i];
        var previousLength = currentLength;
        currentLength += getStrippedText(getPlainText(getFormattedText(word))).length;
        lines.push(createFrame(getPlainText(getFormattedTextAt(text, previousLength, currentLength)), getFadeIn(), getStay(), getFadeOut()));
        currentLength++;
    }
    return lines;
};
var colorTypeGenerator = function(text, colors) {
    var lines = [];
    for (var i = 0; colors.length > i; i++) {
        lines.push(createFrame(colors[i] + text, getFadeIn(), getStay(), getFadeOut()));
    }
    return lines;
};
var setDisplay = function(text) {
    var itemList = getFormattedText(text);
    document.getElementById('real-time-display').innerHTML = "";
    for (var e = 0; itemList.length > e; e++) {
        document.getElementById('real-time-display').appendChild(itemList[e]);
    }
};
var setAdditionType = function(addType) {
    var animType = $('#animationType');
    var oldText = animType.text();
    animType.text(addType);
    var marquee = $('#marquee-settings'),
        typer = $('#typer-settings'),
        color = $('#color-settings');
    switch (oldText.toLowerCase()) {
        case 'marquee generator':
            marquee.collapse('hide');
            break;
        case 'text typer generator':
            typer.collapse('hide');
            break;
        case 'color adder generator':
            color.collapse('hide');
            break;
    }
    switch (addType.toLowerCase()) {
        case 'marquee generator':
            marquee.collapse('show');
            break;
        case 'text typer generator':
            typer.collapse('show');
            break;
        case 'color adder generator':
            color.collapse('show');
            break;
    }
};
var running = false,
    lines = [],
    index = 0;
var addLines = function(generatedLines) {
    for (var i = 0; generatedLines.length > i; i++) {
        lines.push(generatedLines[i]);
    }
    addRows()
};
var addRows = function() {
    clearRows();
    for (var i = 0; lines.length > i; i++) {
        createRow(lines[i], i, lines.length)
    }
};
var generateText = function() {
    var textField = $('#animationText');
    var lastDisplay = $('#editPreview');
    var text = textField.val();
    var type = $('#animationType').text().toLowerCase();
    switch (type) {
        case 'plain text':
            addLines([createFrame(text, getFadeIn(), getStay(), getFadeOut())]);
            textField.val("");
            lastDisplay.val('');
            break;
        case 'marquee generator':
            var marqueeWidth = $('#marquee-width').val();
            if (isNumber(marqueeWidth)) {
                addLines(marqueeGenerator(text, parseInt(marqueeWidth)));
                textField.val("");
                lastDisplay.val('');
            } else {
                errorPopup('The width size needs to be a number!')
            }
            break;
        case 'text typer generator':
            var stepSizeString = $('#typer-step-size').val();
            if (isNumber(stepSizeString)) {
                addLines(textTypeGenerator(text, parseInt(stepSizeString)));
                textField.val("");
                lastDisplay.val('');
            } else {
                errorPopup('The step size needs to be a number!')
            }
            break;
        case 'word typer generator':
            addLines(wordTypeGenerator(text));
            textField.val("");
            lastDisplay.val('');
            break;
        case 'color adder generator':
            var colorString = $('#colors-colors').val();
            var colors = colorString.split(',');
            if (colors.length > 1) {
                addLines(colorTypeGenerator(text, colors));
                textField.val("");
                lastDisplay.val('');
            } else {
                errorPopup('Please add more than one color! (Remember to separate them by commas!')
            }
            break;
    }
};
var startEngine = function() {
    running = true;
    if (lines == null || lines.length === 0) return;
    engineLoop();
    $("#stop-engine").removeClass("disabled");
    $("#start-engine").addClass("disabled");
    $("#restart-engine").removeClass("disabled");
};
var stopEngine = function() {
    running = false;
    setDisplay("Not running!");
    $("#start-engine").removeClass("disabled");
    $("#stop-engine").addClass("disabled");
    $("#restart-engine").addClass("disabled");
};
var engineTick = function() {
    var i = index % lines.length;
    setDisplay(lines[i].text);
    if (i == 0 && index != 0) {
        index = 0;
    } else {
        index++;
    }
};
var engineLoop = function() {
    if (!running) return;
    setTimeout(function() {
        if (!running) return;
        engineTick();
        engineLoop();
        var frame = lines[index % lines.length];
        stay = (frame.fadeIn + frame.stay + frame.fadeOut) * 50;
    }, stay == null ? 0 : stay)
};
var deleteRow = function(i) {
    lines.splice(i, 1);
    addRows();
    generateConfig();
};
var clearRows = function() {
    $("#animation-lines").empty();
};
var createRow = function(frame, i, length) {
    var main = document.createElement('div');
    main.classList.add("row");
    main.classList.add("clearfix");
    main.classList.add("animation-line");
    main.style.height = '38px';
    main.id = "animation-line-" + i;
    main.innerHTML = '\
    <div class="col-lg-3">\
        <div class="btn-group btn-group-justified">\
            <a href="#" ' + (i == 0 ? '' : ('onClick="lines.moveUp(' + i + '); clearRows(); addRows(); generateConfig();"')) + ' class="btn btn-default' + (i == 0 ? " disabled" : "") + '"><i class="fa fa-arrow-up"></i></a>\
            <a href="#" ' + (i + 1 == length ? '' : ('onClick="lines.moveDown(' + i + '); clearRows(); addRows(); generateConfig();"')) + ' class="btn btn-default' + (i + 1 == length ? " disabled" : "") + '"><i class="fa fa-arrow-down"></i></a>\
        </div>\
    </div>\
    <div id="animation-line-' + i + '-text" class="col-lg-6 text-center mc-colors mc-font" style="height: 38px;">\
    </div>\
    <div class="col-lg-3">\
        <div class="btn-group btn-group-justified">\
            <a href="#" class="btn btn-info" data-toggle="modal" data-target="#animation-frame-modal-' + i + '"><i class="fa fa-pencil"></i></a>\
            <a href="#" onclick="deleteRow(' + i + ')" class="btn btn-danger"><i class="fa fa-minus"></i></a>\
        </div>\
    </div>\
    ';
    var modal = document.createElement('div');
    modal.classList.add('modal');
    modal.classList.add('fade');
    modal.id = 'animation-frame-modal-' + i;
    modal.setAttribute('tabindex', '-1');
    modal.setAttribute('role', 'dialog');
    modal.setAttribute('aria-hidden', 'true');
    modal.innerHTML = '\
    <div class="modal-dialog">\
        <div class="modal-content">\
            <div class="modal-header">\
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>\
                <h4 class="modal-title">Edit frame #' + (i + 1) + '</h4>\
            </div>\
            <div class="modal-body">\
                <div class="well well-lg mc-colors mc-font text-center" id="edit-preview-' + i + '" style="min-height: 75px;"></div>\
                <input type="text" value="' + lines[i].text + '" id="animationText-' + i + '" class="form-control mc-font mc-colors" placeholder="Your animation frame" style="margin-bottom: 20px;">\
                <div class="well well-lg">\
                    <div class="row">\
                        <div class="col-lg-4">\
                            <div class="input-group input-group-sm">\
                                <span class="input-group-addon">Fade In</span>\
                                <input type="number" class="form-control" placeholder="Ticks" id="fade-in-' + i + '" value="' + lines[i].fadeIn + '">\
                            </div>\
                        </div>\
                        <div class="col-lg-4">\
                            <div class="input-group input-group-sm">\
                                <span class="input-group-addon">Stay</span>\
                                <input type="number" class="form-control" placeholder="Ticks" id="stay-' + i + '" value="' + lines[i].stay + '">\
                            </div>\
                        </div>\
                        <div class="col-lg-4">\
                            <div class="input-group input-group-sm">\
                                <span class="input-group-addon">Fade Out</span>\
                                <input type="number" class="form-control" placeholder="Ticks" id="fade-out-' + i + '" value="' + lines[i].fadeOut + '">\
                            </div>\
                        </div>\
                    </div>\
                </div>\
            </div>\
            <div class="modal-footer">\
                <button type="button" class="btn btn-danger" data-dismiss="modal">Close</button>\
                <button type="button" onclick="editFrame(' + i + ');" class="btn btn-success">Save changes</button>\
            </div>\
        </div>\
    </div>';
    $("#animation-lines").append(main);
    $('#animation-line-' + i + '-text').append(getFormattedText(frame.text));
    $('#animation-line-modals').append(modal);
    var loadText = function(lineIndex) {
        var itemList = getFormattedText($('#animationText-' + lineIndex).val());
        var animText = document.getElementById('edit-preview-' + lineIndex);
        animText.innerHTML = "";
        for (var i = 0; itemList.length > i; i++) {
            animText.appendChild(itemList[i]);
        }
    };
    var frameIndex = i;
    $('#animationText-' + i).on('input', function() {
        var itemList = getFormattedText($(this).val());
        var animText = document.getElementById('edit-preview-' + frameIndex);
        animText.innerHTML = "";
        for (var i = 0; itemList.length > i; i++) {
            animText.appendChild(itemList[i]);
        }
    });
    loadText(i);
};
$('#animationText').on('input', function() {
    var itemList = getFormattedText($(this).val());
    var animText = document.getElementById('edit-preview');
    animText.innerHTML = "";
    for (var i = 0; itemList.length > i; i++) {
        animText.appendChild(itemList[i]);
    }
});
$('#animation-name').on('input', function() {
    generateConfig();
});
generateConfig();