package org.wso2.ballerina.plugin

import org.assertj.core.api.Assertions
import org.assertj.core.api.ObjectAssert
import org.jetbrains.kotlin.config.LanguageVersion
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.sonar.api.batch.fs.internal.TestInputFileBuilder
import org.sonar.api.batch.sensor.cpd.internal.TokensLine
import org.sonar.api.batch.sensor.internal.SensorContextTester
import org.wso2.ballerina.converter.Environment
import org.wso2.ballerina.utils.kotlinTreeOf
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.name

class CopyPasteDetectorTest {

    @JvmField
    @TempDir
    var tmpFolder: Path? = null

    @Test
    fun test() {
        val tmpFile = tmpFolder!!.resolve("dummy.bal").createFile()
        val content = """
        /*
         * some licence header for example
         */
        package foo.bar
        
        import java.nio.file.Path
        
        class Bar {
            fun foo() {
                println("something")
            }
        }
        """.trimIndent()

        val sensorContext: SensorContextTester = SensorContextTester.create(tmpFolder!!.root)
        val inputFile = TestInputFileBuilder("moduleKey", tmpFile.name)
            .setContents(content)
            .build()

        val root = kotlinTreeOf(content, Environment(emptyList(), LanguageVersion.LATEST_STABLE), inputFile)
        val ctx = InputFileContextImpl(sensorContext, inputFile, false)
        CopyPasteDetector().scan(ctx, root)

        val cpdTokenLines = sensorContext.cpdTokens(inputFile.key())!!

        Assertions.assertThat(cpdTokenLines).hasSize(5)

        assertThat(cpdTokenLines[0])
            .hasValue("classBar{")
            .hasStartLine(8)
            .hasStartUnit(1)
            .hasEndUnit(3)

        assertThat(cpdTokenLines[1])
            .hasValue("funfoo(){")
            .hasStartLine(9)
            .hasStartUnit(4)
            .hasEndUnit(8)

        assertThat(cpdTokenLines[2])
            .hasValue("""println("LITERAL")""")
            .hasStartLine(10)
            .hasStartUnit(9)
            .hasEndUnit(14)

        assertThat(cpdTokenLines[3])
            .hasValue("}")
            .hasStartLine(11)
            .hasStartUnit(15)
            .hasEndUnit(15)

        assertThat(cpdTokenLines[4])
            .hasValue("}")
            .hasStartLine(12)
            .hasStartUnit(16)
            .hasEndUnit(16)
    }
}

private class TokensLineAssert(actual: TokensLine) : ObjectAssert<TokensLine>(actual) {
    fun hasValue(expected: String) = also {
        Assertions.assertThat(actual.value).isEqualTo(expected)
    }

    fun hasStartLine(expected: Int) = also {
        Assertions.assertThat(actual.startLine).isEqualTo(expected)
    }

    fun hasStartUnit(expected: Int) = also {
        Assertions.assertThat(actual.startUnit).isEqualTo(expected)
    }

    fun hasEndUnit(expected: Int) = also {
        Assertions.assertThat(actual.endUnit).isEqualTo(expected)
    }
}

private fun assertThat(actual: TokensLine): TokensLineAssert {
    return TokensLineAssert(actual)
}