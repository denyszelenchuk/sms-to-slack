package com.slacksms.app.data.rules

import com.slacksms.app.data.AppDatabase
import com.slacksms.app.executors.AppExecutors
import com.slacksms.app.rules.RulesView
import java.lang.ref.WeakReference

@Suppress("LABEL_NAME_CLASH")
class RulesRepository(appExecutors: AppExecutors, database: AppDatabase) {

    private var mDatabase: AppDatabase? = database
    private var mCachedRules: List<Rule>? = null
    private val mAppExecutors: AppExecutors = appExecutors

    fun saveRule(rule: Rule, saveRuleCallback: RulesCallback) {
        val rulesCallback = WeakReference<RulesCallback>(saveRuleCallback)

        mAppExecutors.diskIO().execute(Runnable {
            mDatabase!!.rulesDao().insertRule(rule)
            // notify on the main thread
            mAppExecutors.mainThread().execute(Runnable {
                val callback = rulesCallback.get() ?: return@Runnable
                callback.onRuleSaved(rule)
            })
        })
    }

    fun getRules(getRulesCallback: RulesCallback) {
        val rulesCallback = WeakReference<RulesCallback>(getRulesCallback)

        mAppExecutors.diskIO().execute(Runnable {
            val rules = mDatabase!!.rulesDao().rules
            // notify on the main thread
            mAppExecutors.mainThread().execute(Runnable {
                val callback = rulesCallback.get() ?: return@Runnable
                if (rules.isEmpty()) {
                    callback.onDataNotAvailable()
                } else {
                    mCachedRules = rules
                    callback.onRulesLoaded(mCachedRules!!)
                }
            })
        })
    }

    fun getRulesWhenSmsReceived(getRulesCallback: RulesCallback) {
        val rulesCallback = WeakReference<RulesCallback>(getRulesCallback)

        mAppExecutors.diskIO().execute(Runnable {
            val rules = mDatabase!!.rulesDao().rules
            // notify on the main thread
            mAppExecutors.mainThread().execute(Runnable {
                val callback = rulesCallback.get() ?: return@Runnable
                if (rules.isEmpty()) {
                    callback.onDataNotAvailable()
                } else {
                    mCachedRules = rules
                    callback.onRulesLoaded(mCachedRules!!)
                }
            })
        })
    }

    fun deleteRules(deleteRulesCallback: RulesCallback, rulesView: RulesView) {
        val rulesCallback = WeakReference<RulesCallback>(deleteRulesCallback)

        mAppExecutors.diskIO().execute(Runnable {
            mDatabase!!.rulesDao().deleteRules()
            // notify on the main thread
            mAppExecutors.mainThread().execute(Runnable {
                val mainThreadCallback = rulesCallback.get() ?: return@Runnable
                mainThreadCallback.onRulesDeleted(rulesView)
            })
        })
    }

    fun deleteRule(rule: Rule, deleteRulesCallback: RulesCallback) {
        val rulesCallback = WeakReference<RulesCallback>(deleteRulesCallback)

        mAppExecutors.diskIO().execute(Runnable {
            mDatabase!!.rulesDao().deleteRuleById(rule.getId())
            // notify on the main thread
            mAppExecutors.mainThread().execute(Runnable {
                val mainThreadCallback = rulesCallback.get() ?: return@Runnable
                mainThreadCallback.onRuleDeleted()
            })
        })
    }

    fun getRuleById(ruleId: String?): Rule {
        return mDatabase!!.rulesDao().getRuleById(ruleId)
    }
}
