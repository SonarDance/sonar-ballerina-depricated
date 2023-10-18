package org.wso2.ballerina.api

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.sonar.api.rule.RuleKey
import org.wso2.ballerina.converter.KotlinTextRanges.textRange
import org.wso2.ballerina.plugin.KotlinFileContext
import org.wso2.ballerina.verifier.KotlinVerifier
import java.util.stream.Stream

class AbstractCheckTest {
    class DummyCheck(val reportingFunction: (DummyCheck, KotlinFileContext, PsiElement) -> Unit) : AbstractCheck() {
        override fun visitNamedFunction(node: KtNamedFunction, kotlinFileContext: KotlinFileContext) {
            reportingFunction(this, kotlinFileContext, node)
        }
    }

    class IssueReportingFunProvider : ArgumentsProvider {
        override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> = Stream.of(
            Arguments.of({ check: DummyCheck, kotlinFileContext: KotlinFileContext, node: PsiElement ->
                check.apply {
                    kotlinFileContext.reportIssue(
                        kotlinFileContext.textRange(node),
                        "Hello World!"
                    )
                }
            }),
            Arguments.of({ check: DummyCheck, kotlinFileContext: KotlinFileContext, node: PsiElement ->
                check.apply { kotlinFileContext.reportIssue(node, "Hello World!") }
            }),
            Arguments.of({ check: DummyCheck, kotlinFileContext: KotlinFileContext, node: PsiElement ->
                check.apply {
                    kotlinFileContext.reportIssue(
                        node,
                        "Hello World!",
                        listOf(SecondaryLocation(kotlinFileContext.textRange(21, 22, 23, 24), "secondary 2")),
                        2.2
                    )
                }
            })
        )
    }

    @Test
    fun `initialization test`() {
        val dummyCheck = DummyCheck { _, _, _ -> }
        val ruleKey = RuleKey.of("BallerinaTest", "X99999")
        dummyCheck.initialize(ruleKey)
        assertThat(dummyCheck.ruleKey).isSameAs(ruleKey)
    }

    @ParameterizedTest
    @ArgumentsSource(IssueReportingFunProvider::class)
    fun `report issues in various ways`(reportingFunction: (DummyCheck, KotlinFileContext, PsiElement) -> Unit) {
        KotlinVerifier(
            DummyCheck(reportingFunction).apply { initialize(RuleKey.of("BallerinaTest", "X99999")) }
        ) {
            fileName = "Dummy.bal"
            classpath = emptyList()
        }.verify()
    }
}

