package com.github.breadmoirai.discordtabletop.discord

import dev.minn.jda.ktx.interactions.components.ButtonDefaults
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

/**
 * Create a button with keyword arguments.
 *
 * This will use the defaults from [ButtonDefaults] unless specified as parameters.
 *
 * @param[id] The component id to use.
 * @param[style] The button style.
 * @param[label] The button label
 * @param[emoji] The button emoji
 *
 * @return[Button] The resulting button instance.
 */
fun emoji(
    id: String,
    emoji: Emoji,
    label: String? = ButtonDefaults.LABEL,
    style: ButtonStyle = ButtonDefaults.STYLE,
    disabled: Boolean = ButtonDefaults.DISABLED,
) = Button.of(style, id, label, emoji).withDisabled(disabled)
