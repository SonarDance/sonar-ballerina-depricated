package org.wso2.ballerina

import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.TextPointer
import org.sonar.api.batch.fs.TextRange
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.rule.RuleKey
import org.wso2.ballerina.api.InputFileContext
import org.wso2.ballerina.api.SecondaryLocation

class DummyInputFileContext : InputFileContext {
    override var filteredRules: Map<String, Set<TextRange>> = emptyMap()
    override val inputFile: InputFile = DummyInputFile()
    override val sensorContext: SensorContext
        get() = throw NotImplementedError()
    override val isAndroid: Boolean = false

    val issuesReported = mutableListOf<ReportedIssue>()

    override fun reportIssue(
            ruleKey: RuleKey,
            textRange: TextRange?,
            message: String,
            secondaryLocations: List<SecondaryLocation>,
            gap: Double?
    ) {
        issuesReported.add(ReportedIssue(ruleKey, textRange, message, secondaryLocations, gap))
    }

    override fun reportAnalysisParseError(repositoryKey: String, inputFile: InputFile, location: TextPointer?) {
        throw NotImplementedError()
    }

    override fun reportAnalysisError(message: String?, location: TextPointer?) {
        throw NotImplementedError()
    }

}

data class ReportedIssue(
        val ruleKey: RuleKey,
        val textRange: TextRange?,
        val message: String,
        val secondaryLocations: List<SecondaryLocation>,
        val gap: Double?
)
