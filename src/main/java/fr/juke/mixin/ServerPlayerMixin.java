package fr.juke.mixin;

import fr.juke.death.DeathMessageService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "die", at = @At("HEAD"))
    private void deathmessages$clearCustomFlag(DamageSource source, CallbackInfo ci) {
        DeathMessageService.clearCustomDeathFlag();
    }

    @Redirect(method = "die", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void deathmessages$broadcastSystemMessage(PlayerList playerList, Component message, boolean bypassHiddenChat) {
        if (DeathMessageService.consumeCustomDeathFlag()) {
            DeathMessageService.sendCustomDeath((ServerPlayer) (Object) this, playerList, message);
        } else {
            playerList.broadcastSystemMessage(message, bypassHiddenChat);
        }
    }

    @Redirect(method = "die", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemToTeam(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/network/chat/Component;)V"))
    private void deathmessages$broadcastSystemToTeam(PlayerList playerList, Player player, Component message) {
        if (DeathMessageService.consumeCustomDeathFlag()) {
            DeathMessageService.sendCustomDeath((ServerPlayer) (Object) this, playerList, message);
        } else {
            playerList.broadcastSystemToTeam(player, message);
        }
    }

    @Redirect(method = "die", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemToAllExceptTeam(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/network/chat/Component;)V"))
    private void deathmessages$broadcastSystemToAllExceptTeam(PlayerList playerList, Player player, Component message) {
        if (DeathMessageService.consumeCustomDeathFlag()) {
            DeathMessageService.sendCustomDeath((ServerPlayer) (Object) this, playerList, message);
        } else {
            playerList.broadcastSystemToAllExceptTeam(player, message);
        }
    }
}
