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
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.sonar.check.Rule
import org.wso2.ballerina.api.BYTE_ARRAY_CONSTRUCTOR
import org.wso2.ballerina.api.BYTE_ARRAY_CONSTRUCTOR_SIZE_ARG_ONLY
import org.wso2.ballerina.api.CallAbstractCheck
import org.wso2.ballerina.api.ConstructorMatcher
import org.wso2.ballerina.api.FunMatcherImpl
import org.wso2.ballerina.api.isBytesInitializedFromString
import org.wso2.ballerina.api.isInitializedPredictably
import org.wso2.ballerina.api.matches
import org.wso2.ballerina.api.predictRuntimeIntValue
import org.wso2.ballerina.api.predictRuntimeValueExpression
import org.wso2.ballerina.api.secondaryOf
import org.wso2.ballerina.api.simpleArgExpressionOrNull
import org.wso2.ballerina.plugin.KotlinFileContext

private const val SPECS_PACKAGE = "javax.crypto.spec"
private const val KEY_SPEC_FUN_NAME = "PBEKeySpec"
private const val PARAMETER_SPEC_FUN_NAME = "PBEParameterSpec"

private const val MSG_MAKE_UNPREDICTABLE = "Make this salt unpredictable."
private const val MSG_ADD_SALT = "Add an unpredictable salt value to this hash."
private const val MSG_MIN_LEN = "Make this salt at least 16 bytes."
private const val SMSG_PREDICTABLE_SALT = "Predictable salt value"

private val matcherSaltIndexMap = mapOf(
    ConstructorMatcher("$SPECS_PACKAGE.$KEY_SPEC_FUN_NAME") to 1,
    ConstructorMatcher("$SPECS_PACKAGE.$PARAMETER_SPEC_FUN_NAME") to 0,
)

@Rule(key = "S2053")
class UnpredictableHashSaltCheck : CallAbstractCheck() {
    override val functionsToVisit = matcherSaltIndexMap.keys

    override fun visitFunctionCall(
            callExpression: KtCallExpression,
            resolvedCall: ResolvedCall<*>,
            matchedFun: FunMatcherImpl,
            kotlinFileContext: KotlinFileContext
    ) {
        val saltArgIndex = matcherSaltIndexMap[matchedFun]!!

        if (callExpression.valueArguments.size < 2) {
            kotlinFileContext.reportIssue(callExpression, MSG_ADD_SALT)
            return
        }

        val bindingContext = kotlinFileContext.bindingContext
        val saltArg = resolvedCall.valueArgumentsByIndex?.elementAtOrNull(saltArgIndex)
            ?.arguments?.elementAtOrNull(0)?.getArgumentExpression() ?: return

        val predictedSaltValue = saltArg.predictRuntimeValueExpression(bindingContext)

        if (predictedSaltValue.isBytesInitializedFromString(bindingContext)) {
            kotlinFileContext.reportIssue(
                saltArg,
                MSG_MAKE_UNPREDICTABLE,
                listOf(kotlinFileContext.secondaryOf(predictedSaltValue, SMSG_PREDICTABLE_SALT))
            )
            return
        }

        val saltInitializer = predictedSaltValue.getResolvedCall(bindingContext)
            ?.takeIf { it matches BYTE_ARRAY_CONSTRUCTOR } ?: return

        if (saltInitializer.byteArrayInitSizeTooSmall(bindingContext)) {
            kotlinFileContext.reportIssue(saltArg, MSG_MIN_LEN, listOf(kotlinFileContext.secondaryOf(predictedSaltValue)))
        }

        if (saltInitializer matches BYTE_ARRAY_CONSTRUCTOR_SIZE_ARG_ONLY &&
            saltArg.isInitializedPredictably(predictedSaltValue, bindingContext)
        ) {
            kotlinFileContext.reportIssue(
                saltArg,
                MSG_MAKE_UNPREDICTABLE,
                listOf(kotlinFileContext.secondaryOf(predictedSaltValue, SMSG_PREDICTABLE_SALT))
            )
        }
    }

    /**
     * Checks whether the first argument of the call is an integer that is at least 16
     */
    private fun ResolvedCall<*>.byteArrayInitSizeTooSmall(bindingContext: BindingContext) =
        (this.simpleArgExpressionOrNull(0)?.predictRuntimeIntValue(bindingContext) ?: 16) < 16
}
