#!/bin/sh
#source ../env/bin/activate
set -e

thisFont="JetBrainsMono"  #must match the name in the font file
VF_DIR=../fonts/variable
TT_DIR=../fonts/ttf
OT_DIR=../fonts/otf
WEB_DIR=../fonts/web

#Generating fonts ==========================================================
#Requires fontmake https://github.com/googlefonts/fontmake

echo "CLEAN FONTS FOLDERS"
rm -rf ../fonts
mkdir -p $VF_DIR $TT_DIR $OT_DIR $WEB_DIR

echo ".
GENERATING VARIABLE
."
fontmake -g $thisFont.glyphs -o variable --output-path $VF_DIR/$thisFont[wght].ttf
fontmake -g $thisFont-Italic.glyphs -o variable --output-path $VF_DIR/$thisFont-Italic[wght].ttf

echo ".
GENERATING STATIC TTF
."
fontmake -g $thisFont.glyphs -i -o ttf --output-dir $TT_DIR
fontmake -g $thisFont-Italic.glyphs -i -o ttf --output-dir $TT_DIR

echo ".
GENERATING STATIC OTF
."
fontmake -g $thisFont.glyphs -i -o otf --output-dir $OT_DIR
fontmake -g $thisFont-Italic.glyphs -i -o otf --output-dir $OT_DIR

#Post-processing fonts ======================================================
#Requires gftools https://github.com/googlefonts/gftools
#Requires ttfautohint https://www.freetype.org/ttfautohint/osx.html#homebrew

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

gftools fix-isfixedpitch --fonts $VF_DIR/*.ttf
for font in $vfs
do
	mv $font.fix $font
done


echo ".
POST-PROCESSING TTF
."
ttfs=$(ls $TT_DIR/*.ttf)
echo $ttfs
for font in $ttfs
do
	gftools fix-dsig --autofix $font
	ttfautohint $font $font.fix
	[ -f $font.fix ] && mv $font.fix $font
	gftools fix-hinting $font
	[ -f $font.fix ] && mv $font.fix $font
done

gftools fix-isfixedpitch --fonts $TT_DIR/*.ttf
for font in $ttfs
do
	mv $font.fix $font
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

gftools fix-isfixedpitch --fonts $OT_DIR/*.otf
for font in $otfs
do
	mv $font.fix $font
done


#Build woff and woff2 fonts =================================================
#Requires webfonttools https://github.com/bramstein/homebrew-webfonttools

echo ".
BUILD WEBFONTS
."
for font in $ttfs
do
  woff2_compress $font
  sfnt2woff-zopfli $font
done

woffs=$(ls $TT_DIR/*.woff*)
for font in $woffs
do
	mv $font $WEB_DIR
done

rm -rf master_ufo/ instance_ufo/


echo ".
COMPLETE!
."
