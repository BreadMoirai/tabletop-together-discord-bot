package com.github.breadmoirai.discordtabletop.core.games.mafia

interface RoleBag {
    val drawSize: Int
    fun pickRoles(): List<MafiaRole>
}

class FixedRoleBag(val role: MafiaRole, val amount: Int) : RoleBag {
    override val drawSize: Int = amount
    override fun pickRoles(): List<MafiaRole> {
        return List(amount) { role.copy() }
    }
}

class FixedRoleBagContainer(val bags: List<Pair<RoleBag, Int>>) : RoleBag {
    override val drawSize: Int = bags.sumOf { (bag, count) -> bag.drawSize * count }
    override fun pickRoles(): List<MafiaRole> {
        val result = mutableListOf<MafiaRole>()
        for ((bag, count) in bags) {
            repeat(count) {
                result.addAll(bag.pickRoles())
            }
        }
        return result
    }
}

class RandomRoleBagContainer(val bags: List<RoleBag>, val count: Int, val replacement: Boolean) : RoleBag {
    override val drawSize: Int = bags.first().drawSize * count
    override fun pickRoles(): List<MafiaRole> {
        if (replacement) return List(count) { bags.random() }.flatMap { it.pickRoles() }
        val result = mutableListOf<MafiaRole>()
        var rem = count
        while (rem > bags.size) {
            result.addAll(bags.flatMap { it.pickRoles() })
        }
        if (rem > 0) {
            result.addAll(bags.shuffled().take(rem).flatMap { it.pickRoles() })
        }
        return result
    }
}

