package com.github.breadmoirai.discordtabletop.core.games.mafia

class RoleBagContainer(val bags: List<RoleBag>) : RoleBag {
    override fun pickRoles(amount: Int): List<Role> {
        TODO("Not yet implemented")
    }
}

class RandomRoleBag(val roles: List<Role>, val amount: Int, val replacement: Boolean) : RoleBag {
    override fun pickRoles(): List<Role> {
        if (replacement) return List(amount) { roles.random().copy() }
        if (amount < roles.size) return roles.shuffled().take(amount).map(Role::copy)
        val r = mutableListOf<Role>()
        var a = amount
        while (a > roles.size) {
            r.addAll(roles)
            a -= roles.size
        }
        if (a != 0)
            r.addAll(roles.shuffled().take(a).map(Role::copy))
        return r
    }
}

class FixedRoleBag(val role: Role, val amount: Int) : RoleBag {
    override fun pickRoles(): List<Role> {
        return List(amount) { role.copy() }
    }
}


interface RoleBag {
    fun pickRoles(): List<Role>
}