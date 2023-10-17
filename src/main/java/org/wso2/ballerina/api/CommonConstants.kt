package org.wso2.ballerina.api

const val INT_TYPE = "kotlin.Int"
const val STRING_TYPE = "kotlin.String"
const val ANY_TYPE = "kotlin.Any"
const val GET_INSTANCE = "getInstance"
const val WITH_CONTEXT = "withContext"
const val ASYNC = "async"
const val LAUNCH = "launch"
const val KOTLINX_COROUTINES_PACKAGE = "kotlinx.coroutines"
const val DEFERRED_FQN = "kotlinx.coroutines.Deferred"
const val COROUTINES_FLOW = "kotlinx.coroutines.flow.Flow"
const val COROUTINES_CHANNEL = "kotlinx.coroutines.channels.Channel"
const val THROWS_FQN = "kotlin.jvm.Throws"
const val JAVA_STRING = "java.lang.String"
const val KOTLIN_TEXT = "kotlin.text"
const val JAVA_UTIL_PATTERN = "java.util.regex.Pattern"
const val HASHCODE_METHOD_NAME = "hashCode"
const val EQUALS_METHOD_NAME = "equals"

val BYTE_ARRAY_CONSTRUCTOR = ConstructorMatcher("kotlin.ByteArray")
val BYTE_ARRAY_CONSTRUCTOR_SIZE_ARG_ONLY = ConstructorMatcher("kotlin.ByteArray") { withArguments("kotlin.Int") }

val SECURE_RANDOM_FUNS = FunMatcher(qualifier = "java.security.SecureRandom")

val FUNS_ACCEPTING_DISPATCHERS = listOf(
    FunMatcher(qualifier = KOTLINX_COROUTINES_PACKAGE, name = WITH_CONTEXT),
    FunMatcher(qualifier = KOTLINX_COROUTINES_PACKAGE, name = ASYNC),
    FunMatcher(qualifier = KOTLINX_COROUTINES_PACKAGE, name = LAUNCH),
)
