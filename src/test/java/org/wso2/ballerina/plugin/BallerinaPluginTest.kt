package org.wso2.ballerina.plugin

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.sonar.api.SonarEdition
import org.sonar.api.SonarQubeSide
import org.sonar.api.internal.PluginContextImpl
import org.sonar.api.internal.SonarRuntimeImpl
import org.sonar.api.utils.Version
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class BallerinaPluginTest {
    @Test
    fun test() {
        val runtime = SonarRuntimeImpl.forSonarQube(Version.create(7, 9), SonarQubeSide.SCANNER, SonarEdition.COMMUNITY)
        val context = PluginContextImpl.Builder().setSonarRuntime(runtime).build()
        val ballerinaPlugin = BallerinaPlugin()
        ballerinaPlugin.define(context)
        Assertions.assertThat(context.extensions).hasSize(17)
    }

    @Test
    fun test_sonarlint() {
        val runtime = SonarRuntimeImpl.forSonarLint(Version.create(3, 9))
        val context = PluginContextImpl.Builder().setSonarRuntime(runtime).build()
        val ballerinaPlugin = BallerinaPlugin()
        ballerinaPlugin.define(context)
        Assertions.assertThat(context.extensions).hasSize(4)
    }

    // We have removed this functionality therefore commented out from tests
//    @Test
//    fun test_android_context() {
//        val environment = Environment(listOf("../kotlin-checks-test-sources/build/classes/java/main"), LanguageVersion.LATEST_STABLE)
//
//        Assertions.assertThat(isInAndroidContext(environment)).isTrue
//    }
//
//    @Test
//    fun test_non_android_context() {
//        val environment = Environment(listOf("../kotlin-checks-test-sources/build/classes/kotlin/main"), LanguageVersion.LATEST_STABLE)
//
//        Assertions.assertThat(isInAndroidContext(environment)).isFalse
//    }
}
