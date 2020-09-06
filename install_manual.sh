#!/usr/bin/env bash

echo "Installing JetBrains Mono Fonts"
echo ""
# Get the download URL of the latest release from Github
FONT_URL=$(curl -s https://api.github.com/repos/JetBrains/JetBrainsMono/releases/latest | grep browser_download_url | cut -d '"' -f 4)

# Extract the Zip filename
FONT_ZIP_FILENAME=$(echo $FONT_URL | cut -d '/' -f 9)

# Download the zip file from Github Releases using wget
wget $FONT_URL 1>/dev/null 2>&1

# Unzip and move to fonts directory
unzip $FONT_ZIP_FILENAME -d ~/.local/share/fonts && fc-cache -fv

# Clenaup: Remove the downloaded zip file
rm $FONT_ZIP_FILENAME
