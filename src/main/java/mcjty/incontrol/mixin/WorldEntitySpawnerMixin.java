package mcjty.incontrol.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import mcjty.incontrol.config.GeneralConfiguration;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldEntitySpawner.class)
public abstract class WorldEntitySpawnerMixin {
	
	@WrapOperation(
			method = "findChunksForSpawning",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;isAnyPlayerWithinRangeAt(DDDD)Z")
	)
	private boolean incontrol_vanillaWorldEntitySpawner_findChunksForSpawning_isAnyPlayerWithinRangeAt(WorldServer instance, double x, double y, double z, double range, Operation<Boolean> original, @Local EnumCreatureType creatureType) {
		if(creatureType == EnumCreatureType.MONSTER) return original.call(instance, x, y, z, (double)GeneralConfiguration.MIN_PLAYER_MONSTER_SPAWN_DISTANCE);
		else return original.call(instance, x, y, z, (double)GeneralConfiguration.MIN_PLAYER_MOB_SPAWN_DISTANCE);
	}
}