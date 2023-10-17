package org.wso2.ballerina.plugin

import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.Test
import org.sonar.api.config.internal.MapSettings

internal class BallerinaLanguageTest {

    @Test
    fun test_suffixes_default() {
        val ballerinaLanguage = BallerinaLanguage(MapSettings().asConfig())
        AssertionsForClassTypes.assertThat(ballerinaLanguage.fileSuffixes).containsExactly(".bal")
    }

    @Test
    fun test_suffixes_empty() {
        val ballerinaLanguage =
                BallerinaLanguage(MapSettings().setProperty(BallerinaPlugin.BALLERINA_FILE_SUFFIXES_KEY, "").asConfig())
        AssertionsForClassTypes.assertThat(ballerinaLanguage.fileSuffixes).containsExactly(".bal")
    }

    @Test
    fun test_suffixes_custom() {
        val ballerinaLanguage =
                BallerinaLanguage(MapSettings().setProperty(BallerinaPlugin.BALLERINA_FILE_SUFFIXES_KEY, ".foo, .bar").asConfig())
        AssertionsForClassTypes.assertThat(ballerinaLanguage.fileSuffixes).containsExactly(".foo", ".bar")
    }

    @Test
    fun test_key_and_name() {
        val ballerinaLanguage = BallerinaLanguage(MapSettings().asConfig())
        AssertionsForClassTypes.assertThat(ballerinaLanguage.key).isEqualTo("ballerina")
        AssertionsForClassTypes.assertThat(ballerinaLanguage.name).isEqualTo("Ballerina")
    }
}
