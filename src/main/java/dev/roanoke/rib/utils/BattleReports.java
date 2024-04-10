package dev.roanoke.rib.utils;

import dev.roanoke.rib.Gyms.GymBattle;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BattleReports {
    public static void requestBattleConfirmation(GymBattle gymBattle, ServerPlayerEntity gymLeader, ServerPlayerEntity challenger) {
        Text confirmationMessage = Text.literal("Confirm your battle against " + challenger.getGameProfile().getName() + " was a Gym Battle that should be logged ")
                .append(Text.literal("[Yes]")
                        .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/registergymbattle " + gymBattle.getUuid())).withColor(Formatting.GREEN)))
                .append(Text.literal(" "))
                .append(Text.literal("[No]")
                        .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ignoregymbattle " + gymBattle.getUuid())).withColor(Formatting.RED)));

        gymLeader.sendMessage(confirmationMessage);
    }
}
