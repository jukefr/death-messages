package fr.juke.mixin;

import fr.juke.death.DeathMessageService;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageSource.class)
public abstract class DamageSourceMixin {
    @Inject(method = "getLocalizedDeathMessage", at = @At("HEAD"), cancellable = true)
    private void deathmessages$customDeathMessage(LivingEntity victim, CallbackInfoReturnable<Component> cir) {
        Component custom = DeathMessageService.buildDeathMessage((DamageSource) (Object) this, victim);
        if (custom != null) {
            cir.setReturnValue(custom);
        }
    }
}
