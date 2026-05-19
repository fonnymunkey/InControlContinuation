package mcjty.incontrol.config;

import net.minecraftforge.common.config.Configuration;

public class GeneralConfiguration {

    public static final String CATEGORY_GENERAL = "general";

    public static int MAX_PLAYER_DISTANCE = 100;
    public static float MIN_PLAYER_MONSTER_SPAWN_DISTANCE = 24.0F;
    public static float MIN_PLAYER_MOB_SPAWN_DISTANCE = 24.0F;
    public static boolean ON_JOIN_ENFORCEMENT = true;

    public static void init(Configuration cfg) {
        MAX_PLAYER_DISTANCE = cfg.getInt("maxPlayerDistance", CATEGORY_GENERAL, MAX_PLAYER_DISTANCE, 0, Integer.MAX_VALUE, "The maximum range for finding the nearest player for the spawn rules");
        MIN_PLAYER_MONSTER_SPAWN_DISTANCE = cfg.getFloat("minPlayerMonsterSpawnDistance", CATEGORY_GENERAL, MIN_PLAYER_MONSTER_SPAWN_DISTANCE, 8.0F, 128.0F, "Overrides the Vanilla value for minimum range from a player monster mobs are allowed to spawn naturally at");
        MIN_PLAYER_MOB_SPAWN_DISTANCE = cfg.getFloat("minPlayerMobSpawnDistance", CATEGORY_GENERAL, MIN_PLAYER_MOB_SPAWN_DISTANCE, 8.0F, 128.0F, "Overrides the Vanilla value for minimum range from a player general mobs are allowed to spawn naturally at");
        ON_JOIN_ENFORCEMENT = cfg.getBoolean("onJoinEnforcement", CATEGORY_GENERAL, ON_JOIN_ENFORCEMENT, "Enforces onJoin actions only apply to EntityJoinWorldEvent, not to EntitySpawnEvent, false for existing behavior applying to both");
    }

}
