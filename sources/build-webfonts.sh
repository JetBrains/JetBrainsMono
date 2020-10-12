#!/bin/sh

source env/bin/activate
set -e

#requires https://github.com/bramstein/homebrew-webfonttools

WEB_DIR=./fonts/webfonts
TT_DIR=./fonts/ttf

echo ".
CLEAN FONTS FOLDERS
."
rm -rf $WEB_DIR
mkdir -p $WEB_DIR

echo ".
BUILDDING WEBFONTS
."
ttfs=$(ls $TT_DIR/*.ttf)
for fonts in $ttfs
do
  woff2_compress $fonts
  sfnt2woff-zopfli $fonts
done

webfonts=$(ls $TT_DIR/*.woff*)
for font in $webfonts
do
  mv $font $WEB_DIR
done

echo ".
COMPLETE!
."
