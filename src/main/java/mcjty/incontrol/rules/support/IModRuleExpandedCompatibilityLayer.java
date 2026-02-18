package mcjty.incontrol.rules.support;

import mcjty.tools.rules.IModRuleCompatibilityLayer;
import net.minecraft.world.World;

public interface IModRuleExpandedCompatibilityLayer extends IModRuleCompatibilityLayer {
	
	// --------------------
	// Nyx
	// --------------------
	boolean hasNyx();
	
	boolean isHarvestMoonNyx(World world);
	
	boolean isStarShowerNyx(World world);
	
	boolean isBloodMoonNyx(World world);
	
	boolean isFullMoonNyx(World world);
	
	// --------------------
	// Hyxcate
	// --------------------
	boolean hasHyxcate();
	
	boolean isRedGiantHyxcate(World world);
	
	boolean isGrimEclipseHyxcate(World world);
	
	boolean isBlueMoonHyxcate(World world);
	
	boolean isStarShowerHyxcate(World world);
	
	boolean isBloodMoonHyxcate(World world);
	
	boolean isFullMoonHyxcate(World world);
}