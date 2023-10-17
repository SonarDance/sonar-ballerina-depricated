package org.wso2.ballerina.visiting

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtElement
import org.sonar.api.batch.rule.Checks
import org.wso2.ballerina.api.AbstractCheck
import org.wso2.ballerina.plugin.KotlinFileContext
import org.wso2.ballerina.plugin.MetricsUtils.measureDuration

class KtChecksVisitor(val checks: Checks<out AbstractCheck>) : KotlinFileVisitor() {

    override fun visit(kotlinFileContext: KotlinFileContext) {
        flattenNodes(listOf(kotlinFileContext.ktFile)).let { flatNodes ->
            checks.all().forEach { check ->
                measureDuration(check.javaClass.simpleName) {
                    flatNodes.forEach { node ->
                        // Note: we only visit KtElements. If we need to visit PsiElement, add a
                        // visitPsiElement function in KotlinCheck and call it here in the else branch.
                        when (node) {
                            is KtElement -> node.accept(check, kotlinFileContext)
                        }
                    }
                }
            }
        }
    }

    private tailrec fun flattenNodes(childNodes: List<PsiElement>, acc: MutableList<PsiElement> = mutableListOf()): List<PsiElement> =
        if (childNodes.none()) acc
        else flattenNodes(childNodes = childNodes.flatMap { it.children.asList() }, acc = acc.apply { addAll(childNodes) })
}
