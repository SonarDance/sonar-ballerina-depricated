package org.wso2.ballerina.api

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.sonar.api.batch.fs.TextRange
import org.wso2.ballerina.converter.KotlinTextRanges.textRange
import org.wso2.ballerina.plugin.KotlinFileContext

data class SecondaryLocation(val textRange: TextRange, val message: String? = null)

fun KotlinFileContext.secondaryOf(psiElement: PsiElement, msg: String? = null) = SecondaryLocation(textRange(psiElement), msg)
