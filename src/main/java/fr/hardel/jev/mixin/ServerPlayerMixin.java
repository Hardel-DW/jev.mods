package fr.hardel.jev.mixin;

import fr.hardel.jev.bot.Bots;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void jev$tickBot(CallbackInfo callbackInfo) {
        Bots.tick((ServerPlayer) (Object) this);
    }
}
