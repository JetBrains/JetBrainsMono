import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Update PostScript and PONOSE metadata to make the font monospaced on Windows
 * ttx command is required to run this script
 * @author Konstantin Bulenkov
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun main() {
    File("./ttf/")
            .listFiles { _, name -> name.endsWith(".ttf") }
            .forEach {
                val ttx = it.nameWithoutExtension + ".ttx"
                val dir = it.parentFile
                File(dir, ttx).delete()
                updatePostScriptMetaAndPANOSE(it)
                it.delete()
                "ttx $ttx".runCommand(dir)
                File(dir, ttx).delete()
            }
}

fun updatePostScriptMetaAndPANOSE(file: File) {
    "ttx ${file.name}".runCommand(file.parentFile)
    val ttx = file.parentFile.listFiles { _, name -> name == file.nameWithoutExtension + ".ttx" }?.first()
    val panose = PANOSE_TABLE[file.name]
    if (ttx != null && panose != null) {
        var xml = ttx.readText()
        val start = xml.indexOf("<panose>")
        val end = xml.indexOf("</panose>") + "</panose>".length
        xml = xml.replaceRange(IntRange(start, end), panose.toString())
        xml = xml.replace("<isFixedPitch value=\"0\"/>", "<isFixedPitch value=\"1\"/>")
        ttx.writeText(xml)
    }
}

fun String.runCommand(workingDir: File) {
    ProcessBuilder(*split(" ").toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor(1, TimeUnit.MINUTES)
}

data class PANOSE(val bFamilyType: Int = 2,
                  val bSerifStyle: Int = 1,
                  val bWeight: Int,
                  val bProportion: Int = 9,
                  val bContrast: Int,
                  val bStrokeVariation: Int = 1,
                  val bArmStyle: Int = 2,
                  val bLetterForm: Int = 5,
                  val bMidline: Int = 0,
                  val bXHeight: Int = 4) {

    override fun toString(): String {
        return """
           <panose>
                 <bFamilyType value="$bFamilyType"/>
                 <bSerifStyle value="$bSerifStyle"/>
                 <bWeight value="$bWeight"/>
                 <bProportion value="$bProportion"/>
                 <bContrast value="$bContrast"/>
                 <bStrokeVariation value="$bStrokeVariation"/>
                 <bArmStyle value="$bArmStyle"/>
                 <bLetterForm value="$bLetterForm"/>
                 <bMidline value="$bMidline"/>
                 <bXHeight value="$bXHeight"/>
           </panose>
           """.trimIndent()
    }
}

val PANOSE_TABLE = mapOf(
        "JetBrainsMono-Regular.ttf" to PANOSE(bWeight = 5, bContrast = 2),
        "JetBrainsMono-Italic.ttf" to PANOSE(bWeight = 5, bContrast = 2),
        "JetBrainsMono-Medium.ttf" to PANOSE(bWeight = 6, bContrast = 2),
        "JetBrainsMono-Medium-Italic.ttf" to PANOSE(bWeight = 6, bContrast = 2),
        "JetBrainsMono-Bold.ttf" to PANOSE(bWeight = 8, bContrast = 3),
        "JetBrainsMono-Bold-Italic.ttf" to PANOSE(bWeight = 8, bContrast = 3),
        "JetBrainsMono-ExtraBold.ttf" to PANOSE(bWeight = 9, bContrast = 3),
        "JetBrainsMono-ExtraBold-Italic.ttf" to PANOSE(bWeight = 9, bContrast = 3)
)

