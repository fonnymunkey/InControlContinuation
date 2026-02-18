package mcjty.incontrol.compat;

import de.ellpeck.nyx.capability.NyxWorld;
import de.ellpeck.nyx.event.lunar.NyxEventBloodMoon;
import de.ellpeck.nyx.event.lunar.NyxEventBlueMoon;
import de.ellpeck.nyx.event.lunar.NyxEventFullMoon;
import de.ellpeck.nyx.event.lunar.NyxEventStarShower;
import de.ellpeck.nyx.event.solar.NyxEventGrimEclipse;
import de.ellpeck.nyx.event.solar.NyxEventRedGiant;
import net.minecraft.world.World;

public class HyxcateSupport {
	
	public static boolean isRedGiant(World world) {
		NyxWorld cap = NyxWorld.get(world);
		return cap != null && cap.currentSolarEvent instanceof NyxEventRedGiant;
	}
	
	public static boolean isGrimEclipse(World world) {
		NyxWorld cap = NyxWorld.get(world);
		return cap != null && cap.currentSolarEvent instanceof NyxEventGrimEclipse;
	}
	
	public static boolean isBlueMoon(World world) {
		NyxWorld cap = NyxWorld.get(world);
		return cap != null && cap.currentLunarEvent instanceof NyxEventBlueMoon;
	}
	
	public static boolean isStarShower(World world) {
		NyxWorld cap = NyxWorld.get(world);
		return cap != null && cap.currentLunarEvent instanceof NyxEventStarShower;
	}
	
	public static boolean isBloodMoon(World world) {
		NyxWorld cap = NyxWorld.get(world);
		return cap != null && cap.currentLunarEvent instanceof NyxEventBloodMoon;
	}
	
	public static boolean isFullMoon(World world) {
		NyxWorld cap = NyxWorld.get(world);
		return cap != null && cap.currentLunarEvent instanceof NyxEventFullMoon;
	}
}