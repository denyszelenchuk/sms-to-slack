package com.slacksms.app.data.rules

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RulesDao {

    /**
     * Select all channels from the channels table.
     *
     * @return all channels.
     */
    @get:Query("SELECT * FROM rules")
    val rules: List<Rule>

    @Query("SELECT * FROM rules WHERE channelId = :channelId")
    fun getRulesByChannelId(channelId: String?): List<Rule>

    /**
     * Select a task by id.
     *
     * @param ruleId - the rule id.
     * @return ruleId.
     */
    @Query("SELECT * FROM rules WHERE entryId = :ruleId")
    fun getRuleById(ruleId: String?): Rule

    /**
     * Insert a rule in the database.
     *
     * @param rule the rule to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRule(rule: Rule)

    /**
     * Delete a task by id.
     *
     * @return the number of channels deleted. This should always be 1.
     */
    @Query("DELETE FROM rules WHERE entryId = :ruleId")
    fun deleteRuleById(ruleId: String?): Int

    /**
     * Delete all rules.
     */
    @Query("DELETE FROM rules")
    fun deleteRules()
}
