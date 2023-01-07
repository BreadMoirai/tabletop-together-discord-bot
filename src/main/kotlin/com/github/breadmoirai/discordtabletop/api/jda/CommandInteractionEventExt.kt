package com.github.breadmoirai.discordtabletop.api.jda

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionMapping

/**
 * Finds the first option with the specified name.
 *
 *
 * For [CommandAutoCompleteInteraction], this might be incomplete and unvalidated.
 * Auto-complete interactions happen on incomplete command inputs and are not validated.
 *
 *
 * You can use the second and third parameter overloads to handle optional arguments gracefully.
 * See [.getOption] and [.getOption].
 *
 * @param  name
 * The option name
 *
 * @throws IllegalArgumentException
 * If the name is null or if that option is not provided
 *
 * @return The option with the provided name
 *
 */
fun GenericCommandInteractionEvent.requireOption(name: String): OptionMapping {
    val options = getOptionsByName(name)
    if (options.isEmpty())
        throw IllegalArgumentException("Option by name '${name}' is not provided")
    else return options[0]
}