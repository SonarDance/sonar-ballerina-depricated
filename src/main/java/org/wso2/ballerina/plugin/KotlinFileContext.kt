package org.wso2.ballerina.plugin

import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext
import org.wso2.ballerina.api.InputFileContext
import org.wso2.ballerina.api.regex.RegexCache

data class KotlinFileContext(
        val inputFileContext: InputFileContext,
        val ktFile: KtFile,
        val bindingContext: BindingContext,
        val diagnostics: List<Diagnostic>,
        val regexCache: RegexCache,
)
