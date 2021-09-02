#!/bin/sh
[ -r env/bin/activate ] && . env/bin/activate
set -e

thisFont="JetBrainsMono"  #must match the name in the font file
VF_DIR=./fonts/variable

#Generating fonts ==========================================================
#Requires fontmake https://github.com/googlefonts/fontmake

echo "CLEAN FONTS FOLDERS"
rm -rf $VF_DIR
mkdir -p $VF_DIR

echo ".
GENERATING VARIABLE
."
fontmake -g ./sources/$thisFont.glyphs -o variable --output-path $VF_DIR/$thisFont[wght].ttf
fontmake -g ./sources/$thisFont-Italic.glyphs -o variable --output-path $VF_DIR/$thisFont-Italic[wght].ttf

#Post-processing fonts ======================================================
#Requires gftools https://github.com/googlefonts/gftools

echo ".
POST-PROCESSING VF
."
vfs=$(ls $VF_DIR/*.ttf)
for font in $vfs
do
	gftools fix-dsig --autofix $font
	gftools fix-nonhinting $font $font.fix
	mv $font.fix $font
	gftools fix-unwanted-tables --tables MVAR $font
done
rm $VF_DIR/*gasp*

gftools fix-vf-meta $VF_DIR/$thisFont[wght].ttf $VF_DIR/$thisFont-Italic[wght].ttf
for font in $vfs
do
	mv $font.fix $font
done

rm -rf ./master_ufo/ ./instance_ufo/


echo ".
COMPLETE!
."
