package org.wso2.ballerina.plugin

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition
import org.sonarsource.kotlin.plugin.KotlinProfileDefinition

internal class KotlinProfileDefinitionTest {

    @Test
    fun profile() {
        val context = BuiltInQualityProfilesDefinition.Context()
        KotlinProfileDefinition().define(context)
        val profile = context.profile("ballerina", "Sonar way")
        Assertions.assertThat(profile.rules().size).isGreaterThan(2)
        Assertions.assertThat(profile.rules())
            .extracting<String> { obj: BuiltInQualityProfilesDefinition.BuiltInActiveRule -> obj.ruleKey() }
            .contains("S1764")
    }
}
