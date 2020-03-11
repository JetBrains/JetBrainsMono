[![official JetBrains project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)

# JetBrains Mono
A typeface made for developers. \
More about font features & design can be found on [its page](https://jetbrains.com/mono/).

# **Installation**

### **In JetBrains IDEs**

The most recent version of JetBrains Mono ships with your JetBrains IDE starting with v2019.3.

Select JetBrains Mono in the IDE settings: go to `Preferences/Settings` → `Editor` → `Font`, and then select JetBrains Mono from the Font dropdown.

### Another IDE or an older version of a JetBrains IDE
#### Through brew (MacOS only)
1. Tap the font cask to make the Jetbrains Mono font available :
    ```console
    brew tap homebrew/cask-fonts
    ```
2. Install it using the `font-jetbrains-mono` cask:
   ```console
   brew cask install font-jetbrains-mono
   ```
   
#### Through Chocolatey (Windows only)
1. Install Chocolatey if you haven't done already. See [this page](https://chocolatey.org/install) for instructions on how to do that.
2. In an elevated cmd console (Run as administrator...) :
    ```console
    choco install jetbrainsmono
    ```

#### Or manually
1. [Download font](https://github.com/JetBrains/JetBrainsMono/releases/latest). 
2. Unzip the archive and install the font:
   - Mac. Select all font files in the folder and double-click them. Click the *"Install Font"* button.
   - Windows. Select all font files in the folder, right-click any of them, then pick *"Install"* from the menu.
   - Linux. Open a terminal with <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>T</kbd> and run the following:
   
      ```bash
      unzip <font_file.zip> -d ~/.local/share/fonts
      fc-cache -f -v
      ```

#### Picking the font for your IDE
3. Restart your IDE.
4. Go to `Preferences/Settings` → `Editor` → `Font`, and pick JetBrains Mono from the Font dropdown.

### **Visual Studio Code** 

* Follow the instructions above to step 3.
* Go to the settings editor, from the File menu choose Preferences, Settings or use keyboard shortcut <kbd>Ctrl</kbd>+, (<kbd>Cmd</kbd>+, on Mac).
* In the *"Font Family"* input box type JetBrains Mono, replacing any content.
* To enable ligatures turn on the checkbox in *"Font ligatures"*.

#### **Manually editing settings.json**

Visual Studio Code allows you to also edit the underlying settings.json config file. First open the settings editor as described above, then click the "`{}`" icon, at the top right, to open the *"settings.json"* file.

Then paste the following lines and save the file.

```json
"editor.fontFamily": "JetBrains Mono",
"editor.fontLigatures": true,
```

### **ChromeOS Terminal**
In the terminal:
1. Use the keyboard shortcut <kbd>CTRL</kbd> + <kbd>SHIFT</kbd> + <kbd>P</kbd> to open up settings.
2. Scroll down to "Custom CSS (Inline Text)".
3. Copy & paste the following:

```css
@font-face{
    font-family: 'JetBrains Mono';
    src: url('https://raw.githubusercontent.com/JetBrains/JetBrainsMono/master/web/eot/JetBrainsMono-Regular.eot') format('embedded-opentype'),
         url('https://raw.githubusercontent.com/JetBrains/JetBrainsMono/master/web/woff2/JetBrainsMono-Regular.woff2') format('woff2'),
         url('https://raw.githubusercontent.com/JetBrains/JetBrainsMono/master/web/woff/JetBrainsMono-Regular.woff') format('woff'),
         url('https://raw.githubusercontent.com/JetBrains/JetBrainsMono/master/ttf/JetBrainsMono-Regular.ttf') format('truetype');
    font-weight: normal;
    font-style: normal;
}

* {
    -webkit-font-feature-settings: "liga" on, "calt" on;
    -webkit-font-smoothing: antialiased;
    text-rendering: optimizeLegibility;
    font-family: 'JetBrains Mono';
}
```

## Source files

Can be found in the *"Source"* folder. To open them you will need FontLab 6 or higher.

## License

JetBrains Mono typeface is available under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0) and can be used free of charge, for both commercial and non-commercial purposes. You do not need to give credit to JetBrains, although we will appreciate it very much if you do.

## Credits

**Type designer**\
Philipp Nurullin

**Team lead**\
Konstantin Bulenkov

**Thanks to**\
Nikita Prokopov\
Eugene Auduchinok\
Tatiana Tulupenko\
Dmitrij Batrak\
IntelliJ Platform UX Team\
Web Team
