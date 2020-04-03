import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

/**
 * The script updates ttf metadata and fixes fields that are not possible to fix using FontLab.
 * These fixes include:
 *
 * 1. Update PostScript and PONOSE metadata to make the font monospaced on Windows
 * 2. Proper names for Medium and ExtraBold
 *
 * ttx command is required to run this script
 *
 * @author Konstantin Bulenkov
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun main() {
    File("./ttf/")
            .listFiles { _, name -> name.endsWith(".ttf") }
            .forEach {
                val ttx = it.nameWithoutExtension + ".ttx"
                val dir = it.parentFile
                File(dir, ttx).deleteAndLog()
                updateMetaData(it)
                it.deleteAndLog()
                "ttx $ttx".runCommand(dir)
                File(dir, ttx).deleteAndLog()
            }
}

fun updateMetaData(file: File) {
    "ttx ${file.name}".runCommand(file.parentFile)
    val ttx = file.parentFile.listFiles { _, name -> name == file.nameWithoutExtension + ".ttx" }?.first()
    if (ttx == null) return
    val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val doc = documentBuilder.parse(ttx)
    updateMonospaceFlags(file, doc)
    updateNameRecords(file, doc)
    val transformer = TransformerFactory.newInstance().newTransformer()
    transformer.transform(DOMSource(doc), StreamResult(FileOutputStream(ttx)))
}

fun updateNameRecords(file: File, doc: Document) {
    val xPath = XPathFactory.newInstance().newXPath()
    val nameRecords = NAME_RECORDS[file.name]
    if (nameRecords != null) {
        nameRecords.forEach {
            val nameID = it.nameID
            val platformID = it.platformID
            val path = "/ttFont/name/namerecord[@nameID='$nameID'][@platformID='$platformID']"
            val result = xPath.evaluate(path, doc, XPathConstants.NODE)
            val nameRecord: Node
            if (result == null) {
                val nameNode = xPath.evaluate("/ttFont/name", doc, XPathConstants.NODE) as Node
                nameRecord = doc.createElement("namerecord") as Node
                nameNode.appendChild(nameRecord)
                val nameIDAttr = doc.createAttribute("nameID")
                val platformIDAttr = doc.createAttribute("platformID")
                val langIDAttr = doc.createAttribute("langID")
                val platEncIDAttr = doc.createAttribute("platEncID")
                nameIDAttr.value = nameID.toString()
                platformIDAttr.value = platformID.toString()
                langIDAttr.value = "0x409"
                platEncIDAttr.value = "1"
                nameRecord.attributes.setNamedItem(nameIDAttr)
                nameRecord.attributes.setNamedItem(platformIDAttr)
                nameRecord.attributes.setNamedItem(langIDAttr)
                nameRecord.attributes.setNamedItem(platEncIDAttr)
            } else {
                nameRecord = result as Node
            }
            nameRecord.textContent = it.value
        }
    }
}

fun NodeList.asList(): List<Node> = NodeListWrapper(this)

class NodeListWrapper(val nodeList: NodeList) : AbstractList<Node>(), RandomAccess {
    override val size: Int
        get() = nodeList.length

    override fun get(index: Int): Node = nodeList.item(index)
}

private fun updateMonospaceFlags(file: File, doc: Document) {
    val panose = PANOSE_TABLE[file.name]
    if (panose != null) {
        val xPath = XPathFactory.newInstance().newXPath()
        panose.javaClass.declaredFields.forEach {
            val node = xPath.evaluate("/ttFont/OS_2/panose/${it.name}", doc, XPathConstants.NODE) as Node
            it.isAccessible = true
            (node.attributes.item(0) as Attr).value = it.getInt(panose).toString()
        }
    }
}

////////////////////// Utility functions and data classes //////////////////////

fun String.runCommand(workingDir: File) {
    ProcessBuilder(*split(" ").toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
        .waitFor(1, TimeUnit.MINUTES)
}

fun File.deleteAndLog() {
    if (!exists()) return
    println("Deleting $absolutePath")
    val result = delete()
    println("[$result]".toUpperCase())
    if (!result) deleteOnExit()
}

data class NameRecord(val nameID: Int,
                      val platformID: Int,
                      val value: String);

data class PANOSE(val bFamilyType: Int = 2,
                  val bSerifStyle: Int = 1,
                  val bWeight: Int,
                  val bProportion: Int = 9,
                  val bContrast: Int,
                  val bStrokeVariation: Int = 1,
                  val bArmStyle: Int = 2,
                  val bLetterForm: Int = 5,
                  val bMidline: Int = 0,
                  val bXHeight: Int = 4)

////////////////////// Font data //////////////////////

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

val NAME_RECORDS = mapOf<String, List<NameRecord>>(
    "JetBrainsMono-Regular.ttf" to listOf(
        NameRecord(1,	3,	"JetBrains Mono"),
        NameRecord(2,	3,	"Regular"),
        NameRecord(4,	3,	"JetBrains Mono Regular"),
        NameRecord(6,	3,	"JetBrainsMono-Regular"),
        NameRecord(1,	1,	"JetBrains Mono"),
        NameRecord(2,	1,	"Regular"),
        NameRecord(4,	1,	"JetBrains Mono Regular"),
        NameRecord(6,	1,	"JetBrainsMono-Regular")),

    "JetBrainsMono-Italic.ttf" to listOf(
        NameRecord(1,	3,	"JetBrains Mono"),
        NameRecord(2,	3,	"Italic"),
        NameRecord(4,	3,	"JetBrains Mono Italic"),
        NameRecord(6,	3,	"JetBrainsMono-Italic"),
        NameRecord(1,	1,	"JetBrains Mono"),
        NameRecord(2,	1,	"Italic"),
        NameRecord(4,	1,	"JetBrains Mono Italic"),
        NameRecord(6,	1,	"JetBrainsMono-Italic")),

    "JetBrainsMono-Bold.ttf" to listOf(
        NameRecord(1,	3,	"JetBrains Mono"),
        NameRecord(2,	3,	"Bold"),
        NameRecord(4,	3,	"JetBrains Mono Bold"),
        NameRecord(6,	3,	"JetBrainsMono-Bold"),
        NameRecord(1,	1,	"JetBrains Mono"),
        NameRecord(2,	1,	"Bold"),
        NameRecord(4,	1,	"JetBrains Mono Bold"),
        NameRecord(6,	1,	"JetBrainsMono-Bold")),

    "JetBrainsMono-Bold-Italic.ttf" to listOf(
        NameRecord(1,	3,	"JetBrains Mono"),
        NameRecord(2,	3,	"Bold Italic"),
        NameRecord(4,	3,	"JetBrains Mono Bold Italic"),
        NameRecord(6,	3,	"JetBrainsMono-BoldItalic"),
        NameRecord(1,	1,	"JetBrains Mono"),
        NameRecord(2,	1,	"Bold Italic"),
        NameRecord(4,	1,	"JetBrains Mono Bold Italic"),
        NameRecord(6,	1,	"JetBrainsMono-BoldItalic")),

    "JetBrainsMono-Medium.ttf" to listOf(
        NameRecord(1,	3,	"JetBrains Mono Medium"),
        NameRecord(2,	3,	"Regular"),
        NameRecord(4,	3,	"JetBrains Mono Medium"),
        NameRecord(6,	3,	"JetBrainsMono-Medium"),
        NameRecord(16,	3,	"JetBrains Mono "),
        NameRecord(17,	3,	"Medium"),
        NameRecord(1,	1,	"JetBrains Mono"),
        NameRecord(2,	1,	"Medium"),
        NameRecord(4,	1,	"JetBrains Mono Medium"),
        NameRecord(6,	1,	"JetBrainsMono-Medium")),

    "JetBrainsMono-Medium-Italic.ttf" to listOf(
        NameRecord(1,	3,	"JetBrains Mono Medium"),
        NameRecord(2,	3,	"Italic"),
        NameRecord(4,	3,	"JetBrains Mono Medium Italic"),
        NameRecord(6,	3,	"JetBrainsMono-MediumItalic"),
        NameRecord(16,	3,	"JetBrains Mono "),
        NameRecord(17,	3,	"Medium Italic"),
        NameRecord(1,	1,	"JetBrains Mono"),
        NameRecord(2,	1,	"Medium Italic"),
        NameRecord(4,	1,	"JetBrains Mono Medium Italic"),
        NameRecord(6,	1,	"JetBrainsMono-MediumItalic")),


    "JetBrainsMono-ExtraBold.ttf" to listOf(
        NameRecord(1,	3,	"JetBrains Mono ExtraBold"),
        NameRecord(2,	3,	"Regular"),
        NameRecord(4,	3,	"JetBrains Mono ExtraBold"),
        NameRecord(6,	3,	"JetBrainsMono-ExtraBold"),
        NameRecord(16,	3,	"JetBrains Mono "),
        NameRecord(17,	3,	"ExtraBold"),
        NameRecord(1,	1,	"JetBrains Mono"),
        NameRecord(2,	1,	"ExtraBold"),
        NameRecord(4,	1,	"JetBrains Mono ExtraBold"),
        NameRecord(6,	1,	"JetBrainsMono-ExtraBold")),

    "JetBrainsMono-ExtraBold-Italic.ttf" to listOf(
        NameRecord(1,	3,	"JetBrains Mono ExtraBold"),
        NameRecord(2,	3,	"Italic"),
        NameRecord(4,	3,	"JetBrains Mono ExtraBold Italic"),
        NameRecord(6,	3,	"JetBrainsMono-ExtraBoldItalic"),
        NameRecord(16,	3,	"JetBrains Mono "),
        NameRecord(17,	3,	"ExtraBold Italic"),
        NameRecord(1,	1,	"JetBrains Mono"),
        NameRecord(2,	1,	"ExtraBold Italic"),
        NameRecord(4,	1,	"JetBrains Mono ExtraBold Italic"),
        NameRecord(6,	1,	"JetBrainsMono-ExtraBoldItalic"))
)
