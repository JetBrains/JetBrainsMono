#!/bin/sh
[ -r env/bin/activate ] && . env/bin/activate
set -e

thisFont="JetBrainsMono"  #must match the name in the font file
TT_DIR=./fonts/ttf
OT_DIR=./fonts/otf

#Generating fonts ==========================================================
#Requires fontmake https://github.com/googlefonts/fontmake

echo ".
CLEAN FONTS FOLDERS
."
rm -rf $TT_DIR $OT_DIR
mkdir -p $TT_DIR $OT_DIR

echo ".
GENERATING STATIC TTF
."
fontmake -g ./sources/$thisFont.glyphs -i -o ttf --output-dir $TT_DIR
fontmake -g ./sources/$thisFont-Italic.glyphs -i -o ttf --output-dir $TT_DIR

echo ".
GENERATING STATIC OTF
."
fontmake -g ./sources/$thisFont.glyphs -i -o otf --output-dir $OT_DIR
fontmake -g ./sources/$thisFont-Italic.glyphs -i -o otf --output-dir $OT_DIR

#Post-processing fonts ======================================================
#Requires gftools https://github.com/googlefonts/gftools
#Requires ttfautohint-py https://github.com/fonttools/ttfautohint-py

echo ".
POST-PROCESSING TTF
."
ttfs=$(ls $TT_DIR/*.ttf)
for font in $ttfs
do
	gftools fix-dsig --autofix $font
	python -m ttfautohint $font $font.fix
	[ -f $font.fix ] && mv $font.fix $font
	gftools fix-hinting $font
	[ -f $font.fix ] && mv $font.fix $font
done

echo ".
POST-PROCESSING OTF
."
otfs=$(ls $OT_DIR/*.otf)
for font in $otfs
do
	gftools fix-dsig --autofix $font
	gftools fix-weightclass $font
	[ -f $font.fix ] && mv $font.fix $font
done


rm -rf master_ufo/ instance_ufo/


echo ".
COMPLETE!
."
