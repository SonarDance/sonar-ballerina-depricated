package org.wso2.ballerina.plugin;

import org.jetbrains.kotlin.com.intellij.psi.PsiComment;
import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace;
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.kotlin.kdoc.psi.api.KDoc;
import org.jetbrains.kotlin.psi.KtFileAnnotationList;
import org.jetbrains.kotlin.psi.KtImportDirective;
import org.jetbrains.kotlin.psi.KtImportList;
import org.jetbrains.kotlin.psi.KtPackageDirective;
import org.jetbrains.kotlin.psi.KtStringTemplateEntry;
import org.jetbrains.kotlin.psi.psiUtil.allChildren;
import org.wso2.ballerina.converter.KotlinTextRanges.textRange;
import org.wso2.ballerina.visiting.KotlinFileVisitor;

class CopyPasteDetector : KotlinFileVisitor() {
    override fun visit(kotlinFileContext: KotlinFileContext) {
        val cpdTokens =
            kotlinFileContext.inputFileContext.sensorContext.newCpdTokens().onFile(kotlinFileContext.inputFileContext.inputFile)

        collectCpdRelevantNodes(kotlinFileContext.ktFile).forEach { node ->
            val text = if (node is KtStringTemplateEntry) "LITERAL" else node.text
            cpdTokens.addToken(kotlinFileContext.textRange(node), text)
        }

        cpdTokens.save()
    }

    private fun collectCpdRelevantNodes(node: PsiElement, acc: MutableList<PsiElement> = mutableListOf()): List<PsiElement> {
        if (!isExcludedFromCpd(node)) {
            if ((node is LeafPsiElement && node !is PsiWhiteSpace) || node is KtStringTemplateEntry) {
                acc.add(node)
            } else {
                node.allChildren.forEach { collectCpdRelevantNodes(it, acc) }
            }
        }

        return acc
    }

    private fun isExcludedFromCpd(node: PsiElement) =
        node is KtPackageDirective ||
            node is KtImportList ||
            node is KtImportDirective ||
            node is KtFileAnnotationList ||
            node is PsiWhiteSpace ||
            node is PsiComment ||
            node is KDoc
}
