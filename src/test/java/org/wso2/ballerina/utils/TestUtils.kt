package org.wso2.ballerina.utils

import org.sonar.api.batch.fs.InputFile
import org.wso2.ballerina.api.regex.RegexCache
import org.wso2.ballerina.converter.Environment
import org.wso2.ballerina.converter.KotlinSyntaxStructure
import org.wso2.ballerina.converter.KotlinTree
import org.wso2.ballerina.converter.bindingContext


fun kotlinTreeOf(content: String, environment: Environment, inputFile: InputFile): KotlinTree {
    val (ktFile, document) = KotlinSyntaxStructure.of(content, environment, inputFile)
   
    val bindingContext = bindingContext(
        environment.env,
        environment.classpath,
        listOf(ktFile),
    )

    return KotlinTree(ktFile, document, bindingContext, bindingContext.diagnostics.noSuppression().toList(), RegexCache())
}
