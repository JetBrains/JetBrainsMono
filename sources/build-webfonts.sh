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

# Looking up for '*.ttf' in $TT_DIR and compress them (in parallel)
find $TT_DIR -type f -name '*.ttf' -print0 | xargs -I {} -P0 -0 sh -c "
	fonttools ttLib.woff2 compress {}
"

echo ".
MOVE WEBFONTS TO OWN DIRECTORY
."

# Looking up for '*.woff*' in $TT_DIR and move them to $WEB_DIR (in parallel)
find $TT_DIR -type f -regex '*.woff*' -print0 | xargs -I {} -P0 -0 sh -c "
	mv {} $WEB_DIR
"

echo ".
COMPLETE!
."
