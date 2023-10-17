package org.wso2.ballerina.verifier

import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.TextPointer
import org.sonar.api.batch.fs.TextRange
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.rule.RuleKey
import org.sonarsource.analyzer.commons.checks.verifier.SingleFileVerifier
import org.wso2.ballerina.DummyInputFile
import org.wso2.ballerina.api.AbstractCheck
import org.wso2.ballerina.api.InputFileContext
import org.wso2.ballerina.api.SecondaryLocation
import org.wso2.ballerina.converter.KotlinTextRanges.contains
import org.wso2.ballerina.converter.KotlinTree
import java.util.function.Consumer

internal class TestContext(
        private val verifier: SingleFileVerifier,
        check: AbstractCheck,
        vararg furtherChecks: AbstractCheck,
        override val inputFile: InputFile = DummyInputFile(),
        override val isAndroid: Boolean = false,
) : InputFileContext {
    private val visitor: KtTestChecksVisitor = KtTestChecksVisitor(listOf(check) + furtherChecks)
    override var filteredRules: Map<String, Set<TextRange>> = emptyMap()

    override val sensorContext: SensorContext
        get() = throw NotImplementedError()

    fun scan(root: KotlinTree) {
        visitor.scan(this, root)
    }

    override fun reportIssue(
            ruleKey: RuleKey,
            textRange: TextRange?,
            message: String,
            secondaryLocations: List<SecondaryLocation>,
            gap: Double?,
    ) {
        if (textRange != null &&
            filteredRules.getOrDefault(ruleKey.toString(), emptySet()).any { other: TextRange -> textRange in other }
        ) {
            // Issue is filtered by one of the filter.
            return
        }

        val issue = textRange?.let {
            val start = textRange.start()
            val end = textRange.end()
            verifier.reportIssue(message)
                .onRange(start.line(), start.lineOffset() + 1, end.line(), end.lineOffset())
        } ?: verifier.reportIssue(message).onFile()
        issue.withGap(gap)
        secondaryLocations.forEach(Consumer { secondary: SecondaryLocation ->
            issue.addSecondary(
                secondary.textRange.start().line(),
                secondary.textRange.start().lineOffset() + 1,
                secondary.textRange.end().line(),
                secondary.textRange.end().lineOffset(),
                secondary.message)
        })
    }

    override fun reportAnalysisParseError(repositoryKey: String, inputFile: InputFile, location: TextPointer?) {
        throw NotImplementedError()
    }

    override fun reportAnalysisError(message: String?, location: TextPointer?) {
        throw NotImplementedError()
    }
}
