package org.wso2.ballerina.plugin

import org.assertj.core.api.Assertions
import org.jetbrains.kotlin.config.LanguageVersion
import org.junit.jupiter.api.Test
import org.wso2.ballerina.converter.Environment

internal class StatementsVisitorTest {

    @Test
    fun `should count top level without declarations and blocks`() {
        val content = """
            package abc
            import xyz
            
            class A{}
            fun bar() = if (a) println()
            fun foo() {}
            """.trimIndent()
        Assertions.assertThat(statements(content)).isEqualTo(2)
    }

    @Test
    fun `should count statements inside blocks`() {
        val content = """
            fun foo(a: Boolean) {
              foo()
              if (a) { 
                foo()
                bar()
              }
              class A{}
              fun bar(){}
            }
            fun f() {
              var a = 2
              var b = 3 
            }""".trimIndent()
        Assertions.assertThat(statements(content)).isEqualTo(6)
    }
}

private fun statements(content: String): Int {
    val ktFile = Environment(emptyList(), LanguageVersion.LATEST_STABLE).ktPsiFactory.createFile(content)
    val statementsVisitor = StatementsVisitor()
    ktFile.accept(statementsVisitor)
    return statementsVisitor.statements
}
