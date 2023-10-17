package org.wso2.ballerina.testing

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import org.sonar.api.batch.fs.TextRange

class TextRangeAssert(actual: TextRange?) : AbstractAssert<TextRangeAssert?, TextRange?>(actual, TextRangeAssert::class.java) {
    fun hasRange(startLine: Int, startLineOffset: Int, endLine: Int, endLineOffset: Int): TextRangeAssert {
        isNotNull
        actual!!.let { actualNotNull: TextRange ->
            assertThat(actualNotNull.start().line()).isEqualTo(startLine)
            assertThat(actualNotNull.start().lineOffset()).isEqualTo(startLineOffset)
            assertThat(actualNotNull.end().line()).isEqualTo(endLine)
            assertThat(actualNotNull.end().lineOffset()).isEqualTo(endLineOffset)
        }
        return this
    }
}

fun assertTextRange(actual: TextRange?): TextRangeAssert {
    return TextRangeAssert(actual)
}
