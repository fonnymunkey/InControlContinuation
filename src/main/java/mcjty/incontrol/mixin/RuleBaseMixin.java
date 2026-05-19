package mcjty.incontrol.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import mcjty.tools.rules.RuleBase;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RuleBase.class)
public abstract class RuleBaseMixin {
	
	@Shadow(remap = false)
	@Final
	protected Logger logger;
	
	@Redirect(
			method = "getItemsWeighted",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z")
	)
	private boolean incontrol_mcjtytoolsRuleBase_getItemsWeighted(ItemStack instance, @Local(name = "name") String name) {
		if(instance.isEmpty()) logger.log(Level.ERROR, "Potential unknown item '{}'!", name);
		return false;
	}
}