package com.github.breadmoirai.discordtabletop.core.games.frosthaven

import com.github.breadmoirai.discordtabletop.storage.Storable
import com.github.breadmoirai.discordtabletop.storage.StorableId
import com.github.breadmoirai.discordtabletop.storage.StorableTransient
import jetbrains.exodus.entitystore.Entity

data class FHPlayer(
    @StorableId val id: String,
    val personalQuest: PersonalQuest? = null
) : Storable {
    @StorableTransient
    val seenBattleGoals: MutableMap<BattleGoal, Int> = mutableMapOf()

    @StorableTransient
    val chosenBattleGoals: MutableMap<BattleGoal, Int> = mutableMapOf()

    override fun onWrite(entity: Entity) {
        seenBattleGoals.forEach { (bg, count) ->
            entity.setProperty("bg-${bg.name}-seen", count)
        }
        chosenBattleGoals.forEach { (bg, count) ->
            entity.setProperty("bg-${bg.name}-chosen", count)
        }
    }

    override fun onRead(entity: Entity) {
        for (bg in BattleGoal.BATTLE_GOALS) {
            seenBattleGoals[bg] = entity.getProperty("bg-${bg.name}-seen") as? Int ?: 0
            chosenBattleGoals[bg] = entity.getProperty("bg-${bg.name}-chosen") as? Int ?: 0
        }
    }
}
