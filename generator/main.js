var app = require('app');
var BrowserWindow = require('browser-window');

require('crash-reporter').start();
var mainWindow = null;

app.on('window-all-closed', onClosed);
app.on('ready', onReady);

function onClosed() {
  if (process.platform != 'darwin') {
    app.quit();
  }
}

function onReady() {
  mainWindow = new BrowserWindow({
    width: 800,
    height: 600,
    'node-integration': false,
    icon: 'icon.jpg'
  });

  mainWindow.loadUrl('file://' + __dirname + '/main.html');

  mainWindow.openDevTools();

  mainWindow.on('closed', function() {
    mainWindow = null;
  });
}
