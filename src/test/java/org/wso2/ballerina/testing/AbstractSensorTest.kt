package org.wso2.ballerina.testing

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.io.TempDir
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.TestInputFileBuilder
import org.sonar.api.batch.rule.CheckFactory
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder
import org.sonar.api.batch.rule.internal.NewActiveRule
import org.sonar.api.batch.sensor.internal.SensorContextTester
import org.sonar.api.config.internal.MapSettings
import org.sonar.api.measures.FileLinesContext
import org.sonar.api.measures.FileLinesContextFactory
import org.sonar.api.rule.RuleKey
import org.sonar.api.utils.log.LogTesterJUnit5
import org.wso2.ballerina.plugin.BallerinaLanguage
import org.wso2.ballerina.plugin.BallerinaPlugin
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.createTempDirectory

abstract class AbstractSensorTest {

    @JvmField
    @TempDir
    var temp: Path? = null

    protected lateinit var baseDir: Path
    protected lateinit var context: SensorContextTester
    protected var fileLinesContextFactory: FileLinesContextFactory = Mockito.mock(FileLinesContextFactory::class.java)

    @JvmField
    @RegisterExtension
    var logTester = LogTesterJUnit5()

    @BeforeEach
    fun setup() {
        baseDir = createTempDirectory(temp!!)
        context = SensorContextTester.create(baseDir)
        val fileLinesContext = Mockito.mock(FileLinesContext::class.java)
        Mockito.`when`(
            fileLinesContextFactory.createFor(
                ArgumentMatchers.any(
                    InputFile::class.java
                )
            )
        ).thenReturn(fileLinesContext)
    }

    protected fun checkFactory(vararg ruleKeys: String): CheckFactory {
        val builder = ActiveRulesBuilder()
        for (ruleKey in ruleKeys) {
            val newRule = NewActiveRule.Builder()
                .setRuleKey(RuleKey.of(BallerinaPlugin.BALLERINA_REPOSITORY_KEY, ruleKey))
                .setName(ruleKey)
                .build()
            builder.addRule(newRule)
        }
        context.setActiveRules(builder.build())
        return CheckFactory(context.activeRules())
    }

    protected fun createInputFile(relativePath: String, content: String, status: InputFile.Status=InputFile.Status.SAME): InputFile {
        return TestInputFileBuilder("moduleKey", relativePath)
            .setModuleBaseDir(baseDir)
            .setType(InputFile.Type.MAIN)
            .setLanguage(language().key)
            .setCharset(StandardCharsets.UTF_8)
            .setContents(content)
            .setStatus(status)
            .build()
    }

    fun language(): BallerinaLanguage = BallerinaLanguage(MapSettings().asConfig())
}
