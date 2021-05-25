#!/bin/sh
source env/bin/activate
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
GENERATING STATIC TTF & OTF
."
cat <<EOF | xargs -I {} -P0 sh -xc "{}"
# GENERATING STATIC TTF
fontmake -g ./sources/$thisFont.glyphs -i -o ttf --output-dir $TT_DIR --master-dir "{tmp}" --instance-dir "{tmp}"
fontmake -g ./sources/$thisFont-Italic.glyphs -i -o ttf --output-dir $TT_DIR --master-dir "{tmp}" --instance-dir "{tmp}"

# GENERATING STATIC OTF
fontmake -g ./sources/$thisFont.glyphs -i -o otf --output-dir $OT_DIR --master-dir "{tmp}" --instance-dir "{tmp}"
fontmake -g ./sources/$thisFont-Italic.glyphs -i -o otf --output-dir $OT_DIR --master-dir "{tmp}" --instance-dir "{tmp}"
EOF

#Post-processing fonts ======================================================
#Requires gftools https://github.com/googlefonts/gftools
#Requires ttfautohint-py https://github.com/fonttools/ttfautohint-py

echo ".
POST-PROCESSING TTF
."

# Looking up for '*.ttf' in $TT_DIR and do required actions (in parallel)
find $TT_DIR -type f -name '*.ttf' -print0 | xargs -0 -I {} -P0 sh -c "
	gftools fix-dsig --autofix {}
	python -m ttfautohint {} {}.fix
	[ -f {}.fix ] && mv {}.fix {} || true
	gftools fix-hinting {}
	[ -f {}.fix ] && mv {}.fix {} || true
"

echo ".
POST-PROCESSING OTF
."

# Looking up for '*.otf' in $OT_DIR and do required actions (in parallel)
find $OT_DIR -type f -name '*.otf' -print0 | xargs -0 -I {} -P0 sh -c "
	gftools fix-dsig --autofix {}
	gftools fix-weightclass {}
	[ -f {}.fix ] && mv {}.fix {} || true
"

echo ".
COMPLETE!
."
