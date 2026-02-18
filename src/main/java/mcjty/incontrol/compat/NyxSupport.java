package mcjty.incontrol.compat;

import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.lunarevents.BloodMoon;
import de.ellpeck.nyx.lunarevents.FullMoon;
import de.ellpeck.nyx.lunarevents.HarvestMoon;
import de.ellpeck.nyx.lunarevents.StarShower;
import net.minecraft.world.World;

public class NyxSupport {
	
	public static boolean isHarvestMoon(World world) {
		NyxWorld cap = NyxWorld.get(world);
		return cap != null && cap.currentEvent instanceof HarvestMoon;
	}
	
	public static boolean isStarShower(World world) {
		NyxWorld cap = NyxWorld.get(world);
		return cap != null && cap.currentEvent instanceof StarShower;
	}
	
	public static boolean isBloodMoon(World world) {
		NyxWorld cap = NyxWorld.get(world);
		return cap != null && cap.currentEvent instanceof BloodMoon;
	}
	
	public static boolean isFullMoon(World world) {
		NyxWorld cap = NyxWorld.get(world);
		return cap != null && cap.currentEvent instanceof FullMoon;
	}
}