package org.wso2.ballerina.plugin

import org.jetbrains.kotlin.config.LanguageVersion
import org.junit.jupiter.api.Test
import org.sonar.api.batch.fs.internal.TestInputFileBuilder
import org.sonarsource.analyzer.commons.checks.verifier.SingleFileVerifier
import org.wso2.ballerina.api.AbstractCheck
import org.wso2.ballerina.checks.BadClassNameCheck
import org.wso2.ballerina.checks.BadFunctionNameCheck
import org.wso2.ballerina.checks.TooManyCasesCheck
import org.wso2.ballerina.checks.UnusedLocalVariableCheck
import org.wso2.ballerina.checks.VariableAndParameterNameCheck
import org.wso2.ballerina.converter.Comment
import org.wso2.ballerina.converter.CommentAnnotationsAndTokenVisitor
import org.sonarsource.kotlin.plugin.IssueSuppressionVisitor
import org.wso2.ballerina.converter.Environment
import org.wso2.ballerina.utils.kotlinTreeOf
import org.wso2.ballerina.verifier.TestContext
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import org.wso2.ballerina.verifier.DEFAULT_KOTLIN_CLASSPATH
import org.wso2.ballerina.verifier.KOTLIN_BASE_DIR

class IssueSuppressionVisitorTest {
    @Test
    fun `verify we actually suppress issues on various AST nodes`() {
        val withSuppressionTestFile = KOTLIN_BASE_DIR.resolve("../sample/IssueSuppressionSample.bal")
        val forNoSuppressionTestFile = KOTLIN_BASE_DIR.resolve("../sample/IssueNonSuppressionSample.bal")
        val withoutSuppressionTestFile = KOTLIN_BASE_DIR.resolve("../sample/IssueWithoutSuppressionSample.bal")
        scanWithSuppression(withSuppressionTestFile).assertOneOrMoreIssues()
        scanWithoutSuppression(forNoSuppressionTestFile).assertOneOrMoreIssues()
        scanWithSuppression(withoutSuppressionTestFile).assertOneOrMoreIssues()
    }

    private fun scanWithSuppression(path: Path) =
        scanFile(path, true, BadClassNameCheck(), BadFunctionNameCheck(), VariableAndParameterNameCheck(), UnusedLocalVariableCheck(), TooManyCasesCheck())

    private fun scanWithoutSuppression(path: Path) =
        scanFile(path, false, BadClassNameCheck(), BadFunctionNameCheck(), VariableAndParameterNameCheck(), UnusedLocalVariableCheck(), TooManyCasesCheck())

    private fun scanFile(path: Path, suppress: Boolean, check: AbstractCheck, vararg checks: AbstractCheck): SingleFileVerifier {
        val env = Environment(DEFAULT_KOTLIN_CLASSPATH, LanguageVersion.LATEST_STABLE)
        val verifier = SingleFileVerifier.create(path, StandardCharsets.UTF_8)
        val testFileContent = String(Files.readAllBytes(path), StandardCharsets.UTF_8)
        val inputFile = TestInputFileBuilder("moduleKey",  "src/org/foo/kotlin.bal")
            .setCharset(StandardCharsets.UTF_8)
            .initMetadata(testFileContent)
            .build()

        val root = kotlinTreeOf(testFileContent, env, inputFile)

        CommentAnnotationsAndTokenVisitor(root.document, inputFile)
            .apply { visitElement(root.psiFile) }.allComments
            .forEach { comment: Comment ->
                val start = comment.range.start()
                verifier.addComment(start.line(), start.lineOffset() + 1, comment.text, 2, 0)
            }
        val ctx = TestContext(verifier, check, *checks)

        if (suppress) IssueSuppressionVisitor().scan(ctx, root)

        ctx.scan(root)
        return verifier
    }
}
