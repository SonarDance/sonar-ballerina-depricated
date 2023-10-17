package org.wso2.ballerina.verifier

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtElement
import org.sonar.api.rule.RuleKey
import org.wso2.ballerina.api.AbstractCheck
import org.wso2.ballerina.plugin.KotlinFileContext
import org.wso2.ballerina.visiting.KotlinFileVisitor

class KtTestChecksVisitor(private val checks: List<AbstractCheck>) : KotlinFileVisitor() {
    init {
        checks.forEach { it.initialize(RuleKey.of("Kotlin", "Dummy")) }
    }

    override fun visit(kotlinFileContext: KotlinFileContext) {
        flattenNodes(listOf(kotlinFileContext.ktFile)).forEach { psiElement ->
            // Note: we only visit KtElements. If we need to visit PsiElement, add a
            // visitPsiElement function in KotlinCheck and call it here in the else branch.
            when (psiElement) {
                is KtElement -> checks.forEach { check -> psiElement.accept(check, kotlinFileContext) }
            }
        }
    }

    private fun flattenNodes(root: List<PsiElement>): List<PsiElement> =
        root + root.flatMap { flattenNodes(it.children.toList()) }
}
