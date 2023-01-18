import glob
import os
from fontTools.ttLib import TTFont

for filepath in glob.iglob('fonts/variable/*.ttf'):
	f = TTFont(filepath)
	f.flavor = 'woff2'
	print('INFO:fontTools.ttLib.woff2:Building WOFF2 for ' + filepath)
	f.save(os.path.splitext(filepath)[0] + '.woff2')
