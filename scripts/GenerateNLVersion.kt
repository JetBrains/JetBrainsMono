import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

/**
 * The scripts generates no ligature version of JetBrains Mono called JetBrains Mono NL
 *
 * ttx command is required to run this script
 *
 * @author Konstantin Bulenkov
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun main() {
    File("./fonts/ttf/")
            .listFiles { _, name -> name.endsWith(".ttf") && !name.startsWith("JetBrainsMonoNL") }
            .forEach {
                val ttx = it.nameWithoutExtension + ".ttx"
                val dir = it.parentFile
                File(dir, ttx).deleteAndLog()
                val doc = ttf2Document(it)
                File(dir, ttx).deleteAndLog()
                if (doc != null) {
                    generateNoLigaturesFont(File(dir, it.name), doc)
                }
            }
}

fun ttf2Document(file: File): Document? {
    "ttx ${file.name}".runCommand(file.parentFile)
    val ttx = file.parentFile.listFiles { _, name -> name == "${file.nameWithoutExtension}.ttx" }?.first() ?: return null
    val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    return documentBuilder.parse(ttx)
}

fun generateNoLigaturesFont(file: File, doc: Document) {
    val nlName = file.nameWithoutExtension.replace("JetBrainsMono", "JetBrainsMonoNL")
    val ttx = "$nlName.ttx"
    val ttf = "$nlName.ttf"
    val dir = file.parentFile
    File(dir, ttf).deleteAndLog()
    doc.removeLigas("/ttFont/GlyphOrder", "GlyphID")
    doc.removeLigas("/ttFont/glyf", "TTGlyph")
    doc.removeLigas("/ttFont/hmtx", "mtx")
    doc.removeLigas("/ttFont/post/extraNames", "psName")
    doc.removeLigas("/ttFont/GDEF/GlyphClassDef", "ClassDef", attName = "glyph")
    doc.removeNode("/ttFont/GPOS")
    doc.removeNode("/ttFont/GSUB")

    val xPath = XPathFactory.newInstance().newXPath()
    val nameRecords = (xPath.evaluate("/ttFont/name/namerecord", doc, XPathConstants.NODESET) as NodeList).asList()
    nameRecords.forEach {
        if (!it.textContent.contains("trademark")) {
            it.textContent = it.textContent
                .replace("JetBrains Mono", "JetBrains Mono NL")
                .replace("JetBrainsMono", "JetBrainsMonoNL")
        }
    }

    val ttxFile = File(dir, ttx)
    doc.saveAs(ttxFile)
    "ttx $ttx".runCommand(dir)
    ttxFile.deleteAndLog()
}

class NodeListWrapper(val nodeList: NodeList) : AbstractList<Node>(), RandomAccess {
    override val size: Int
        get() = nodeList.length

    override fun get(index: Int): Node = nodeList.item(index)
}

////////////////////// Utility functions and data classes //////////////////////

fun NodeList.asList(): List<Node> = NodeListWrapper(this)

fun String.runCommand(workingDir: File) {
    ProcessBuilder(*split(" ").toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
        .waitFor(1, TimeUnit.MINUTES)
}

fun Document.saveAs(file: File) {
    val transformer = TransformerFactory.newInstance().newTransformer()
    transformer.transform(DOMSource(this), StreamResult(FileOutputStream(file)))
}

fun Document.removeLigas(parentPath: String, nodeName: String, attName:String = "name") {
    val xPath = XPathFactory.newInstance().newXPath()
    val parent = xPath.evaluate(parentPath, this, XPathConstants.NODE) as Node
    val nodeFilter = "$parentPath/$nodeName[substring(@$attName, string-length(@$attName)-4) = '.liga']"
    val nodes = (xPath.evaluate(nodeFilter, this, XPathConstants.NODESET) as NodeList).asList()
    nodes.forEach { parent.removeChild(it) }
}

fun Document.removeNode(path: String) {
    val xPath = XPathFactory.newInstance().newXPath()
    val parent = xPath.evaluate(path.substringBeforeLast("/"), this, XPathConstants.NODE)
    if (parent is Node) {
        val child = xPath.evaluate(path, this, XPathConstants.NODE)
        if (child is Node) {
            parent.removeChild(child)
        }
    }
}

fun File.deleteAndLog() {
    if (!exists()) return
    println("Deleting $absolutePath")
    val result = delete()
    println("[$result]".toUpperCase())
    if (!result) deleteOnExit()
}