package org.wso2.ballerina.converter

import org.jetbrains.kotlin.com.intellij.openapi.editor.Document
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiErrorElement
import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.resolve.BindingContext
import org.sonar.api.batch.fs.InputFile
import org.wso2.ballerina.api.ParseException
import org.wso2.ballerina.api.regex.RegexCache
import org.wso2.ballerina.converter.KotlinTextRanges.textPointerAtOffset

class KotlinTree(
        val psiFile: KtFile,
        val document: Document,
        val bindingContext: BindingContext,
        val diagnostics: List<Diagnostic>,
        val regexCache: RegexCache,
)

data class KotlinSyntaxStructure(val ktFile: KtFile, val document: Document, val inputFile: InputFile) {
    companion object {
        @JvmStatic
        fun of(content: String, environment: Environment, inputFile: InputFile): KotlinSyntaxStructure {

            val psiFile: KtFile = environment.ktPsiFactory.createFile(inputFile.uri().path, normalizeEol(content))

            val document = try {
                psiFile.viewProvider.document ?: throw ParseException("Cannot extract document")
            } catch (e: AssertionError) {
                // A KotlinLexerException may occur when attempting to read invalid files
                throw ParseException("Cannot correctly map AST with a null Document object")
            }

            checkParsingErrors(psiFile, document, inputFile)

            return KotlinSyntaxStructure(psiFile, document, inputFile)
        }
    }
}

fun checkParsingErrors(psiFile: PsiFile, document: Document, inputFile: InputFile) {
    descendants(psiFile)
        .firstOrNull { it is PsiErrorElement }
        ?.let { element: PsiElement ->
            throw ParseException(
                "Cannot convert file due to syntactic errors",
                inputFile.textPointerAtOffset(document, element.startOffset)
            )
        }
}

private fun descendants(element: PsiElement): Sequence<PsiElement> {
    return element.children.asSequence().flatMap { tree: PsiElement -> sequenceOf(tree) + descendants(tree) }
}

private fun normalizeEol(content: String) = content.replace("""\r\n?""".toRegex(), "\n")
