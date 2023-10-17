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
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.util.getCall
import org.sonar.check.Rule
import org.wso2.ballerina.api.ArgumentMatcher
import org.wso2.ballerina.api.CallAbstractCheck
import org.wso2.ballerina.api.FunMatcher
import org.wso2.ballerina.api.FunMatcherImpl
import org.wso2.ballerina.api.INT_TYPE
import org.wso2.ballerina.api.predictReceiverExpression
import org.wso2.ballerina.api.predictRuntimeIntValue
import org.wso2.ballerina.api.predictRuntimeStringValue
import org.wso2.ballerina.api.predictRuntimeValueExpression
import org.wso2.ballerina.plugin.KotlinFileContext

private val PREPARE_STATEMENT = FunMatcher(qualifier = "java.sql.Connection", name = "prepareStatement")

private val PREPARE_STATEMENT_SET = FunMatcher(qualifier = "java.sql.PreparedStatement", nameRegex = "^set.*+".toRegex()) {
    withArguments(ArgumentMatcher(INT_TYPE), ArgumentMatcher.ANY)
}

private val RESULT_SET_GET = FunMatcher(qualifier = "java.sql.ResultSet", nameRegex = "^get.*+".toRegex()) {
    withArguments(ArgumentMatcher(INT_TYPE))
    withArguments(ArgumentMatcher(INT_TYPE), ArgumentMatcher.ANY)
}

@Rule(key = "S2695")
class PreparedStatementAndResultSetCheck : CallAbstractCheck() {

    override val functionsToVisit = listOf(PREPARE_STATEMENT_SET, RESULT_SET_GET)

    override fun visitFunctionCall(
            callExpression: KtCallExpression,
            resolvedCall: ResolvedCall<*>,
            matchedFun: FunMatcherImpl,
            kotlinFileContext: KotlinFileContext,
    ) {
        val bindingContext = kotlinFileContext.bindingContext

        val firstArgument = callExpression.valueArguments[0].getArgumentExpression()!!
        val receiver = callExpression.predictReceiverExpression(bindingContext) ?: return
        val firstArgumentValue = firstArgument.predictRuntimeIntValue(bindingContext) ?: return

        when (matchedFun) {
            RESULT_SET_GET ->
                if (firstArgumentValue == 0) kotlinFileContext.reportIssue(firstArgument, "ResultSet indices start at 1.")

            PREPARE_STATEMENT_SET -> {
                if (firstArgumentValue == 0) {
                    kotlinFileContext.reportIssue(firstArgument, "PreparedStatement indices start at 1.")
                } else {
                    getNumberOfParameters(bindingContext, receiver)?.let {
                        if (firstArgumentValue > it) {
                            kotlinFileContext.reportIssue(
                                firstArgument,
                                """This "PreparedStatement" ${if (it == 0) "has no" else "only has $it"} parameters."""
                            )
                        }
                    }
                }
            }
        }

    }
}

private fun getNumberOfParameters(bindingContext: BindingContext, receiver: KtExpression) =
    receiver.predictRuntimeValueExpression(bindingContext)
        .getCall(bindingContext)?.let {
            if (PREPARE_STATEMENT.matches(it, bindingContext)) {
                it.valueArguments[0].getArgumentExpression()
                    ?.predictRuntimeStringValue(bindingContext)
                    ?.count { c -> c.toString() == "?" }
            } else null
        }
