package org.wso2.ballerina.api

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.wso2.ballerina.plugin.KotlinFileContext

abstract class CallAbstractCheck : AbstractCheck() {
    abstract val functionsToVisit: Iterable<FunMatcherImpl>

    open fun visitFunctionCall(
            callExpression: KtCallExpression,
            resolvedCall: ResolvedCall<*>,
            matchedFun: FunMatcherImpl,
            kotlinFileContext: KotlinFileContext
    ) = visitFunctionCall(callExpression, resolvedCall, kotlinFileContext)

    open fun visitFunctionCall(callExpression: KtCallExpression, resolvedCall: ResolvedCall<*>, kotlinFileContext: KotlinFileContext) = Unit

    final override fun visitCallExpression(callExpression: KtCallExpression, kotlinFileContext: KotlinFileContext) {
        val resolvedCall = callExpression.getResolvedCall(kotlinFileContext.bindingContext) ?: return
        functionsToVisit.firstOrNull { resolvedCall matches it }
            ?.let { visitFunctionCall(callExpression, resolvedCall, it, kotlinFileContext) }
    }
}
