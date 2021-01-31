package com.slacksms.app.rules

import com.slacksms.app.data.rules.Rule

interface RulesView {

    /**
     * Display a rules on the screen
     *
     * @param loadedRulesList the list of rules
     */
    fun showRules(loadedRulesList: List<Rule>)

    fun loadRules(loadedRulesList: List<Rule>)

    /**
     * Hide the userName field.
     */
    fun hideRules()
}
