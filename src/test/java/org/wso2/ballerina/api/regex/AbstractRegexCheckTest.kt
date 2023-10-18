package org.wso2.ballerina.api.regex

import org.junit.jupiter.api.Test
import org.sonarsource.analyzer.commons.regex.RegexIssueLocation
import org.sonarsource.analyzer.commons.regex.RegexParseResult
import org.sonarsource.analyzer.commons.regex.ast.CharacterClassTree
import org.sonarsource.analyzer.commons.regex.ast.RegexBaseVisitor
import org.wso2.ballerina.verifier.KotlinVerifier

private class ReportEveryRegexDummyCheck : AbstractRegexCheck() {
    override fun visitRegex(regex: RegexParseResult, regexContext: RegexContext) {
        regexContext.reportIssue(regex.result, "Flags: ${regex.result.activeFlags().mask}")
    }
}

private class ReportEveryRegexDummyCheck2 : AbstractRegexCheck() {
    private var counter = 0

    override fun visitRegex(regex: RegexParseResult, regexContext: RegexContext) {
        regexContext.reportIssue(
            regex.result,
            "Flags: ${regex.result.activeFlags().mask}",
            if (counter == 0) null else counter,
            emptyList()
        )
        counter++
    }

}

private class ReportCharacterClassRegexDummyCheck : AbstractRegexCheck() {
    override fun visitRegex(regex: RegexParseResult, regexContext: RegexContext) {
        val trees = mutableListOf<CharacterClassTree>()
        regex.result.accept(object : RegexBaseVisitor() {
            override fun visitCharacterClass(tree: CharacterClassTree) {
                trees.add(tree)
            }
        })
        regexContext.reportIssue(trees[0], "Character class found", trees.drop(1).map { RegexIssueLocation(it, "+1") })
    }
}
