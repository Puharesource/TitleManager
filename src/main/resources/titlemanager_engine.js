const Bukkit = Java.type('org.bukkit.Bukkit');
const ChatColor = Java.type('org.bukkit.ChatColor');

const createCommandSender = () => ScriptCommandSender.newInstance()

const isTesting = () => Bukkit.getServer() === null

const getTimingsPattern = () => /^\[([-]?\d+);([-]?\d+);([-]?\d+)](.+)$/g

const tmResult = (text, done, fadeIn, stay, fadeOut) => Java.to([text, done, fadeIn, stay, fadeOut])

const hasTimings = (text) => text.match(getTimingsPattern()) !== null

const getTimings = (text) => {
    const match = getTimingsPattern().exec(text)

    let fadeIn = parseInt(match[1])
    let stay = parseInt(match[2])
    let fadeOut = parseInt(match[3])
    let groupText = match[4]

    return [groupText, fadeIn, stay, fadeOut]
}

const parseInt = (string, radix) => {
    let val = NaN
    const Integer = Java.type('java.lang.Integer')

    try {
        if (radix) {
            val = Integer.parseInt(string, radix)
        } else {
            val = Integer.parseInt(string)
        }
    } catch (e) {}

    return val
}

const sendCommand = (commandSender, commandLine) => {
    if (isTesting()) {
        print("Sending command with command line: " + commandLine)
    } else {
        Bukkit.dispatchCommand(commandSender, commandLine)
    }
}