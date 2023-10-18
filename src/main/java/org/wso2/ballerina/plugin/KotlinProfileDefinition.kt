/*
 * SonarSource Kotlin
 * Copyright (C) 2018-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.wso2.ballerina.plugin

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition
import org.sonarsource.analyzer.commons.BuiltInQualityProfileJsonLoader

class KotlinProfileDefinition : BuiltInQualityProfilesDefinition {
    companion object {
        const val PATH_TO_JSON = "org/sonar/l10n/ballerina/rules/ballerina/Sonar_way_profile.json"
    }

    override fun define(context: BuiltInQualityProfilesDefinition.Context) {
        context.createBuiltInQualityProfile(BallerinaPlugin.PROFILE_NAME, BallerinaPlugin.BALLERINA_LANGUAGE_KEY).let { profile ->
            BuiltInQualityProfileJsonLoader.load(profile, BallerinaPlugin.BALLERINA_REPOSITORY_KEY, PATH_TO_JSON)
            profile.done()
        }
    }
}
