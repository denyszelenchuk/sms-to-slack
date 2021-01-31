package com.slacksms.app.data.rules

import androidx.annotation.MainThread
import com.slacksms.app.rules.RulesView

/**
 * Callback called when rule is loaded from the repository.
 */
interface RulesCallback {

    @MainThread
    fun onRuleLoaded(rule: Rule)

    @MainThread
    fun onRuleSaved(rule: Rule)

    @MainThread
    fun onRulesLoaded(rules: List<Rule>)

    @MainThread
    fun onRulesDeleted(rulesView: RulesView)

    @MainThread
    fun onRuleDeleted()

    @MainThread
    fun onDataNotAvailable()
}
