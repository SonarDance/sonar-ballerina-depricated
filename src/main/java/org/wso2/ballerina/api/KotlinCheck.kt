package org.wso2.ballerina.api

import org.sonar.api.rule.RuleKey

interface KotlinCheck {
    fun initialize(ruleKey: RuleKey)
}
