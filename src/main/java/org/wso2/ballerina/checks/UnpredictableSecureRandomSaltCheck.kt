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
package org.wso2.ballerina.checks

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.sonar.check.Rule
import org.wso2.ballerina.api.ArgumentMatcher
import org.wso2.ballerina.api.BYTE_ARRAY_CONSTRUCTOR_SIZE_ARG_ONLY
import org.wso2.ballerina.api.CallAbstractCheck
import org.wso2.ballerina.api.ConstructorMatcher
import org.wso2.ballerina.api.FunMatcher
import org.wso2.ballerina.api.isBytesInitializedFromString
import org.wso2.ballerina.api.isInitializedPredictably
import org.wso2.ballerina.api.matches
import org.wso2.ballerina.api.predictRuntimeValueExpression
import org.wso2.ballerina.api.secondaryOf
import org.wso2.ballerina.api.simpleArgExpressionOrNull
import org.wso2.ballerina.plugin.KotlinFileContext

private const val MESSAGE = "Change this seed value to something unpredictable, or remove the seed."
private const val SECURE_RANDOM = "java.security.SecureRandom"

@Rule(key = "S4347")
class UnpredictableSecureRandomSaltCheck : CallAbstractCheck() {
    override val functionsToVisit = listOf(
        FunMatcher(qualifier = SECURE_RANDOM, name = "setSeed"),
        ConstructorMatcher(SECURE_RANDOM, arguments = listOf(listOf(ArgumentMatcher.ANY)))
    )

    override fun visitFunctionCall(callExpression: KtCallExpression, resolvedCall: ResolvedCall<*>, kotlinFileContext: KotlinFileContext) {
        val bindingContext = kotlinFileContext.bindingContext

        val saltArg = resolvedCall.simpleArgExpressionOrNull(0) ?: return
        val predictedSaltValue = saltArg.predictRuntimeValueExpression(bindingContext)

        if (predictedSaltValue is KtConstantExpression || predictedSaltValue.isBytesInitializedFromString(bindingContext)) {
            kotlinFileContext.reportIssue(saltArg, MESSAGE, listOf(kotlinFileContext.secondaryOf(predictedSaltValue)))
        } else if (
            predictedSaltValue.getResolvedCall(bindingContext) matches BYTE_ARRAY_CONSTRUCTOR_SIZE_ARG_ONLY &&
            saltArg.isInitializedPredictably(predictedSaltValue, bindingContext)
        ) {
            kotlinFileContext.reportIssue(saltArg, MESSAGE, listOf(kotlinFileContext.secondaryOf(predictedSaltValue)))
        }
    }
}
