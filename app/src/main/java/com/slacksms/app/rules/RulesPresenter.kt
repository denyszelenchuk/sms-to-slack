package com.slacksms.app.rules

import com.slacksms.app.data.rules.Rule
import com.slacksms.app.data.rules.RulesCallback
import com.slacksms.app.data.rules.RulesRepository

class RulesPresenter {

    constructor(dataSource: RulesRepository, view: RulesView) {
        rulesRepository = dataSource
        mView = view

        rulesCallback = createRulesCallback()
    }

    fun start() {
        rulesRepository.getRules(rulesCallback)
    }

    fun load() {
        rulesRepository.getRulesWhenSmsReceived(rulesCallback)
    }

    fun stop() {
        mView = null
    }

    fun createRulesCallback(): RulesCallback {
        return object : RulesCallback {

            override fun onRuleDeleted() {
                start()
            }

            override fun onRuleLoaded(rule: Rule) {
                // do nothing
            }

            override fun onRuleSaved(rule: Rule) {
                rule.getId()
            }

            override fun onRulesLoaded(rules: List<Rule>) {
                if (mView != null) {
                    mView!!.showRules(rules)
                }
            }

            override fun onRulesDeleted(rulesView: RulesView) {
                rulesView.hideRules()
            }

            override fun onDataNotAvailable() {
                if (mView != null) {
                    mView!!.hideRules()
                }
            }
        }
    }

    companion object {
        private lateinit var rulesRepository: RulesRepository
        private var mView: RulesView? = null
        private lateinit var rulesCallback: RulesCallback
    }
}
