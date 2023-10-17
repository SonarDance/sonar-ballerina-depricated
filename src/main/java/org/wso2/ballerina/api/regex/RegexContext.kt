package org.wso2.ballerina.api.regex

import org.jetbrains.kotlin.psi.KtStringTemplateEntry
import org.sonarsource.analyzer.commons.regex.RegexIssueLocation
import org.sonarsource.analyzer.commons.regex.RegexParseResult
import org.sonarsource.analyzer.commons.regex.RegexParser
import org.sonarsource.analyzer.commons.regex.ast.FlagSet
import org.sonarsource.analyzer.commons.regex.ast.RegexSyntaxElement
import org.wso2.ballerina.plugin.KotlinFileContext

class RegexCache(
    private val globalCache: MutableMap<Pair<List<KtStringTemplateEntry>, Int>, RegexParseResult> = mutableMapOf()
) {
    fun addIfAbsent(
            stringTemplateEntries: Iterable<KtStringTemplateEntry>,
            flags: FlagSet,
            regexSource: KotlinAnalyzerRegexSource,
    ) =
        globalCache.computeIfAbsent(stringTemplateEntries.toList() to flags.mask) {
            RegexParser(regexSource, flags).parse()
        }
}

class RegexContext(
        private val stringTemplateEntries: Iterable<KtStringTemplateEntry>,
        kotlinFileContext: KotlinFileContext,
) {
    private val cache: RegexCache = kotlinFileContext.regexCache

    private val _reportedIssues = mutableListOf<ReportedIssue>()
    val reportedIssues: List<ReportedIssue>
        get() = _reportedIssues

    val regexSource = KotlinAnalyzerRegexSource(
        stringTemplateEntries,
        kotlinFileContext.inputFileContext.inputFile,
        kotlinFileContext.ktFile.viewProvider.document!!
    )

    fun parseRegex(flags: FlagSet) = cache.addIfAbsent(stringTemplateEntries, flags, regexSource)

    fun reportIssue(
        regexElement: RegexSyntaxElement,
        message: String,
        secondaryLocations: List<RegexIssueLocation> = emptyList(),
        gap: Double? = null
    ) = _reportedIssues.add(ReportedIssue(regexElement, message, secondaryLocations, gap))

    fun reportIssue(
        regexElement: RegexSyntaxElement,
        message: String,
        gap: Int?,
        secondaryLocations: List<RegexIssueLocation>
    ) = reportIssue(regexElement, message, secondaryLocations, gap?.toDouble())
}

data class ReportedIssue(
    val regexElement: RegexSyntaxElement,
    val message: String,
    val secondaryLocations: List<RegexIssueLocation>,
    val gap: Double?
)
