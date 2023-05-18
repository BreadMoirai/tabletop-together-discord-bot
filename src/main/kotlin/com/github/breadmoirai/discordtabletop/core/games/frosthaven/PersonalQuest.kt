package com.github.breadmoirai.discordtabletop.core.games.frosthaven

import com.github.breadmoirai.discordtabletop.storage.Storable
import com.github.breadmoirai.discordtabletop.util.get
import com.github.breadmoirai.discordtabletop.storage.StorableId

data class PersonalQuest(@StorableId val num: Int) : Storable {
    val front = "$endpoint/fh-pq-${"%02d".format(num)}.png"

    companion object {
        private val endpoint: String =
            "https://github.com/any2cards/worldhaven/blob/master/images/personal-quests/frosthaven"
        val PERSONAL_QUESTS: List<PersonalQuest> = listOf(
            PersonalQuest(1),
            PersonalQuest(2),
            PersonalQuest(3),
            PersonalQuest(4),
            PersonalQuest(5),
            PersonalQuest(6),
            PersonalQuest(7),
            PersonalQuest(8),
            PersonalQuest(9),
            PersonalQuest(10),
            PersonalQuest(11),
            PersonalQuest(12),
            PersonalQuest(13),
            PersonalQuest(14),
            PersonalQuest(15),
            PersonalQuest(16),
            PersonalQuest(17),
            PersonalQuest(18),
            PersonalQuest(19),
            PersonalQuest(20),
            PersonalQuest(21),
            PersonalQuest(22),
            PersonalQuest(23)
        )

        fun startingQuests(): List<PersonalQuest> {
            return PERSONAL_QUESTS[0..<10].toList()
        }
    }
}