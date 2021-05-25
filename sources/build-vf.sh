#!/bin/sh
source env/bin/activate
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
cat <<EOF | xargs -I {} -P0 sh -c "{}"
fontmake -g ./sources/$thisFont.glyphs -o variable --output-path $VF_DIR/$thisFont[wght].ttf --master-dir "{tmp}" --instance-dir "{tmp}"
fontmake -g ./sources/$thisFont-Italic.glyphs -o variable --output-path $VF_DIR/$thisFont-Italic[wght].ttf --master-dir "{tmp}" --instance-dir "{tmp}"
EOF

#Post-processing fonts ======================================================
#Requires gftools https://github.com/googlefonts/gftools

echo ".
POST-PROCESSING VF
."

# Looking for '*.ttf' in $VF_DIR and do required actions (in parallel)
find $VF_DIR -type f -name '*.ttf' -print0 | xargs -0 -I {} -P0 sh -c "
	gftools fix-dsig --autofix {};
	gftools fix-nonhinting {} {}.fix
	mv {}.fix {}
	gftools fix-unwanted-tables --tables MVAR {}
"

rm $VF_DIR/*gasp*

gftools fix-vf-meta $VF_DIR/$thisFont[wght].ttf $VF_DIR/$thisFont-Italic[wght].ttf

# Looking for '*.ttf' in $VF_DIR and rename them from 'NAME.fix' to 'NAME' (in parallel)
find $VF_DIR -type f -name '*.ttf' -print0 | xargs -0 -I {} -P0 sh -c "
	mv {}.fix {}
"

echo ".
COMPLETE!
."
