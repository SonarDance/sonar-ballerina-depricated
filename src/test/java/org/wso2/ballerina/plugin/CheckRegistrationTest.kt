package org.wso2.ballerina.plugin

import io.mockk.spyk
import io.mockk.verify
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.junit.jupiter.api.Test
import org.sonar.api.batch.sensor.issue.internal.DefaultNoSonarFilter
import org.sonar.check.Rule
import org.wso2.ballerina.api.AbstractCheck
import org.wso2.ballerina.testing.AbstractSensorTest
import kotlin.time.ExperimentalTime

class CheckRegistrationTest : AbstractSensorTest() {

    @Rule(key = "S99999")
    class DummyCheck : AbstractCheck() {
        override fun visitNamedFunction(function: KtNamedFunction, data: KotlinFileContext?) {
        }
    }

    @ExperimentalTime
    @Test
    fun ensure_check_registration_works() {
        val inputFile = createInputFile("file1.bal", """
            fun main(args: Array<String>) {
                print (1 == 1);
            }
             """.trimIndent())
        context.fileSystem().add(inputFile)
        val dummyCheck = spyk(DummyCheck())
        BallerinaSensor(checkFactory("S99999"), fileLinesContextFactory, DefaultNoSonarFilter(), language()).also { sensor ->
            sensor.checks.addAnnotatedChecks(dummyCheck)
            sensor.execute(context)
        }

        verify(exactly = 1) { dummyCheck.visitNamedFunction(any(), any()) }
    }
}
