package org.wso2.ballerina.visiting

import org.wso2.ballerina.api.InputFileContext
import org.wso2.ballerina.converter.KotlinTree
import org.wso2.ballerina.plugin.KotlinFileContext

abstract class KotlinFileVisitor {
    fun scan(fileContext: InputFileContext, root: KotlinTree){
        visit(KotlinFileContext(fileContext, root.psiFile, root.bindingContext, root.diagnostics, root.regexCache))
    }

    abstract fun visit(kotlinFileContext: KotlinFileContext)
}
