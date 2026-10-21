package ai.niva.rag.ingest

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class ExtractedDocument(
    val fullText: String,
    val pageTexts: List<Pair<Int, String>>,
)

@Singleton
class DocumentTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun extract(uri: Uri, mimeType: String, localFile: File): ExtractedDocument {
        return when {
            mimeType.contains("pdf") -> extractPdf(localFile)
            mimeType.contains("markdown") || localFile.extension.equals("md", true) ->
                ExtractedDocument(readText(localFile), emptyList())
            else -> ExtractedDocument(readText(localFile), emptyList())
        }
    }

    private fun readText(file: File): String = file.readText(Charsets.UTF_8)

    private fun extractPdf(file: File): ExtractedDocument {
        PDFBoxResourceLoader.init(context)
        PDDocument.load(file).use { document ->
            val stripper = PDFTextStripper()
            val pages = mutableListOf<Pair<Int, String>>()
            for (page in 1..document.numberOfPages) {
                stripper.startPage = page
                stripper.endPage = page
                pages.add(page to stripper.getText(document).trim())
            }
            val full = pages.joinToString("\n\n") { it.second }
            return ExtractedDocument(full, pages)
        }
    }
}
