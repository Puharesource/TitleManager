var isNumber;

isNumber = function(number) {
    return !isNaN(parseFloat(number)) && isFinite(number);
};

Array.prototype.moveUp = function(i) {
    var value = this[i], newPos = i - 1;

    this.splice(i, 1);
    if(i === 0)
        newPos = this.length;
    this.splice(newPos, 0, value);
};

Array.prototype.moveDown = function(i) {
    var value = this[i], newPos = i + 1;

    this.splice(i, 1);
    if(i >= this.length)
        newPos = 0;
    this.splice(newPos, 0, value);
};