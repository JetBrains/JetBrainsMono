#!/bin/sh
[ -r env/bin/activate ] && . env/bin/activate
set -e

#requires brotli

WEB_DIR=./fonts/webfonts
TT_DIR=./fonts/ttf

echo ".
CLEAN FONTS FOLDERS
."
rm -rf $WEB_DIR
mkdir -p $WEB_DIR

echo ".
BUILDING WEBFONTS
."
ttfs=$(ls $TT_DIR/*.ttf)
for font in $ttfs
do
	fonttools ttLib.woff2 compress $font
done

echo ".
MOVE WEBFONTS TO OWN DIRECTORY
."
webfonts=$(ls $TT_DIR/*.woff*)
for font in $webfonts
do
  mv $font $WEB_DIR
done

echo ".
COMPLETE!
."
