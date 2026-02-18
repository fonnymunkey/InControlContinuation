package mcjty.incontrol.rules.support;

import com.google.common.base.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.incontrol.InControl;
import mcjty.incontrol.compat.ModRuleCompatibilityLayer;
import mcjty.incontrol.rules.PotentialSpawnRule;
import mcjty.tools.cache.StructureCache;
import mcjty.tools.rules.IEventQuery;
import mcjty.tools.typed.AttributeMap;
import mcjty.tools.typed.Key;
import mcjty.tools.varia.LookAtTools;
import mcjty.tools.varia.Tools;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import static mcjty.incontrol.rules.support.RuleKeys.*;

public class GenericRuleEvaluator<T> {
    protected final List<BiFunction<T, IEventQuery<T>, Boolean>> checks = new ArrayList<>();
    private final Logger logger;
    private final IModRuleExpandedCompatibilityLayer compatibility;
    private final Random rnd = new Random();
    
    public GenericRuleEvaluator(AttributeMap map) {
        this.logger = InControl.setup.getLogger();
        this.compatibility = new ModRuleCompatibilityLayer();
        addChecks(map);
    }

    protected void addChecks(AttributeMap map) {
        if(map.has(HOSTILE)) addHostileCheck(map);
        if(map.has(PASSIVE)) addPassiveCheck(map);
        
        if(map.has(SPAWNER)) addSpawnerCheck(map);
        
        if(map.has(PLAYER)) addPlayerCheck(map);
        if(map.has(MOB)) addMobsCheck(map);
        
        if(map.has(EXPLOSION)) addExplosionCheck(map);
        if(map.has(PROJECTILE)) addProjectileCheck(map);
        if(map.has(FIRE)) addFireCheck(map);
        if(map.has(MAGIC)) addMagicCheck(map);
        
        if(map.has(RANDOM)) addRandomCheck(map);
        if(map.has(DIMENSION)) addDimensionCheck(map);
        if(map.has(MINTIME)) addMinTimeCheck(map);
        if(map.has(MAXTIME)) addMaxTimeCheck(map);
        
        if(map.has(MINHEIGHT)) addMinHeightCheck(map);
        if(map.has(MAXHEIGHT)) addMaxHeightCheck(map);
        if(map.has(WEATHER)) addWeatherCheck(map);
        if(map.has(TEMPCATEGORY)) addTempCategoryCheck(map);
        if(map.has(DIFFICULTY)) addDifficultyCheck(map);
        
        if(map.has(SOURCE)) addSourceCheck(map);
        
        if(map.has(MINSPAWNDIST)) addMinSpawnDistCheck(map);
        if(map.has(MAXSPAWNDIST)) addMaxSpawnDistCheck(map);
        if(map.has(MINPLAYERDIST)) addMinPlayerDistCheck(map);
        if(map.has(MAXPLAYERDIST)) addMaxPlayerDistCheck(map);
        
        if(map.has(MINLIGHT)) addMinLightCheck(map);
        if(map.has(MAXLIGHT)) addMaxLightCheck(map);
        
        if(map.has(MINDIFFICULTY)) addMinAdditionalDifficultyCheck(map);
        if(map.has(MAXDIFFICULTY)) addMaxAdditionalDifficultyCheck(map);
        
        if(map.has(SEESKY)) addSeeSkyCheck(map);
        if(map.has(BLOCK)) addBlocksCheck(map);
        if(map.has(BIOME)) addBiomesCheck(map);
        if(map.has(BIOME_REG)) addBiomeRegCheck(map);
        if(map.has(BIOMETYPE)) addBiomeTypesCheck(map);
        
        if(map.has(SUMMER)) {
            if(compatibility.hasSereneSeasons()) addSummerCheck(map);
            else logger.warn("Serene Seaons is missing: this test cannot work!");
        }
        if(map.has(WINTER)) {
            if(compatibility.hasSereneSeasons()) addWinterCheck(map);
            else logger.warn("Serene Seaons is missing: this test cannot work!");
        }
        if(map.has(SPRING)) {
            if(compatibility.hasSereneSeasons()) addSpringCheck(map);
            else logger.warn("Serene Seaons is missing: this test cannot work!");
        }
        if(map.has(AUTUMN)) {
            if(compatibility.hasSereneSeasons()) addAutumnCheck(map);
            else logger.warn("Serene Seaons is missing: this test cannot work!");
        }
        
        if(map.has(GAMESTAGE)) {
            if(compatibility.hasGameStages()) addGameStageCheck(map);
            else logger.warn("Game Stages is missing: the 'gamestage' test cannot work!");
        }
        
        if(map.has(HARVEST_MOON)) {
            if(compatibility.hasNyx()) addHarvestMoonCheck(map);
            else logger.warn("Nyx is missing: the 'harvestmoon' test cannot work!");
        }
        if(map.has(STAR_SHOWER)) {
            if(compatibility.hasNyx() || compatibility.hasHyxcate()) addStarShowerCheck(map);
            else logger.warn("Nyx/Hyxcate is missing: the 'starshower' test cannot work!");
        }
        if(map.has(BLOOD_MOON)) {
            if(compatibility.hasNyx() || compatibility.hasHyxcate()) addBloodMoonCheck(map);
            else logger.warn("Nyx/Hyxcate is missing: the 'bloodmoon' test cannot work!");
        }
        if(map.has(FULL_MOON)) {
            if(compatibility.hasNyx() || compatibility.hasHyxcate()) addFullMoonCheck(map);
            else logger.warn("Nyx/Hyxcate is missing: the 'fullmoon' test cannot work!");
        }
        if(map.has(RED_GIANT)) {
            if(compatibility.hasHyxcate()) addRedGiantCheck(map);
            else logger.warn("Hyxcate is missing: the 'redgiant' test cannot work!");
        }
        if(map.has(GRIM_ECLIPSE)) {
            if(compatibility.hasHyxcate()) addGrimEclipseCheck(map);
            else logger.warn("Hyxcate is missing: the 'grimeclipse' test cannot work!");
        }
        if(map.has(BLUE_MOON)) {
            if(compatibility.hasHyxcate()) addBlueMoonCheck(map);
            else logger.warn("Hyxcate is missing: the 'bluemoon' test cannot work!");
        }
        
        if(map.has(HELMET)) addHelmetCheck(map);
        if(map.has(CHESTPLATE)) addChestplateCheck(map);
        if(map.has(LEGGINGS)) addLeggingsCheck(map);
        if(map.has(BOOTS)) addBootsCheck(map);
        if(map.has(PLAYER_HELDITEM)) addHeldItemCheck(map, PLAYER_HELDITEM);
        if(map.has(HELDITEM)) addHeldItemCheck(map, HELDITEM);
        if(map.has(OFFHANDITEM)) addOffHandItemCheck(map);
        if(map.has(BOTHHANDSITEM)) addBothHandsItemCheck(map);
        
        if(map.has(AMULET)) {
            if(compatibility.hasBaubles()) addBaubleCheck(map, AMULET, compatibility::getAmuletSlots);
            else logger.warn("Baubles is missing: this test cannot work!");
        }
        if(map.has(RING)) {
            if(compatibility.hasBaubles()) addBaubleCheck(map, RING, compatibility::getRingSlots);
            else logger.warn("Baubles is missing: this test cannot work!");
        }
        if(map.has(BELT)) {
            if(compatibility.hasBaubles()) addBaubleCheck(map, BELT, compatibility::getBeltSlots);
            else logger.warn("Baubles is missing: this test cannot work!");
        }
        if(map.has(TRINKET)) {
            if(compatibility.hasBaubles()) addBaubleCheck(map, TRINKET, compatibility::getTrinketSlots);
            else logger.warn("Baubles is missing: this test cannot work!");
        }
        if(map.has(HEAD)) {
            if(compatibility.hasBaubles()) addBaubleCheck(map, HEAD, compatibility::getHeadSlots);
            else logger.warn("Baubles is missing: this test cannot work!");
        }
        if(map.has(BODY)) {
            if(compatibility.hasBaubles()) addBaubleCheck(map, BODY, compatibility::getBodySlots);
            else logger.warn("Baubles is missing: this test cannot work!");
        }
        if(map.has(CHARM)) {
            if(compatibility.hasBaubles()) addBaubleCheck(map, CHARM, compatibility::getCharmSlots);
            else logger.warn("Baubles is missing: this test cannot work!");
        }
        
        if(map.has(STRUCTURE)) addStructureCheck(map);
        
        if(map.has(INCITY)) {
            if(compatibility.hasLostCities()) addInCityCheck(map);
            else logger.warn("The Lost Cities is missing: the 'incity' test cannot work!");
        }
        if(map.has(INSTREET)) {
            if(compatibility.hasLostCities()) addInStreetCheck(map);
            else logger.warn("The Lost Cities is missing: the 'instreet' test cannot work!");
        }
        if(map.has(INSPHERE)) {
            if(compatibility.hasLostCities()) addInSphereCheck(map);
            else logger.warn("The Lost Cities is missing: the 'insphere' test cannot work!");
        }
        if(map.has(INBUILDING)) {
            if(compatibility.hasLostCities()) addInBuildingCheck(map);
            else logger.warn("The Lost Cities is missing: the 'inbuilding' test cannot work!");
        }
        
        if(map.has(CANSPAWNHERE)) addCanSpawnHereCheck(map);
        if(map.has(NOTCOLLIDING)) addNotCollidingCheck(map);

        if(map.has(REALPLAYER)) addRealPlayerCheck(map);
        if(map.has(FAKEPLAYER)) addFakePlayerCheck(map);
        if(map.has(MOD)) addModsCheck(map);
        if(map.has(MINCOUNT)) addMinCountCheck(map);
        if(map.has(MAXCOUNT)) addMaxCountCheck(map);
    }
    
    private void addHostileCheck(AttributeMap map) {
        if(map.get(HOSTILE)) checks.add((event, query) -> query.getEntity(event) instanceof IMob);
        else checks.add((event, query) -> !(query.getEntity(event) instanceof IMob));
    }
    
    private void addPassiveCheck(AttributeMap map) {
        if(map.get(PASSIVE)) checks.add((event, query) -> (query.getEntity(event) instanceof IAnimals && !(query.getEntity(event) instanceof IMob)));
        else checks.add((event, query) -> !(query.getEntity(event) instanceof IAnimals && !(query.getEntity(event) instanceof IMob)));
    }
    
    private void addSpawnerCheck(AttributeMap map) {
        boolean c = map.get(SPAWNER);
        if(c) {
            checks.add((event, query) -> {
                if(event instanceof LivingSpawnEvent.CheckSpawn) return ((LivingSpawnEvent.CheckSpawn)event).isSpawner();
                else return false;
            });
        }
        else {
            checks.add((event, query) -> {
                if(event instanceof LivingSpawnEvent.CheckSpawn) return !((LivingSpawnEvent.CheckSpawn)event).isSpawner();
                else return false;
            });
        }
    }
    
    private void addPlayerCheck(AttributeMap map) {
        boolean asPlayer = map.get(PLAYER);
        if(asPlayer) checks.add((event, query) -> query.getAttacker(event) instanceof EntityPlayer);
        else checks.add((event, query) -> query.getAttacker(event) instanceof EntityPlayer);
    }
    
    private void addMobsCheck(AttributeMap map) {
        List<String> mobs = map.getList(MOB);
        if(mobs.size() == 1) {
            String name = mobs.get(0);
            String id = PotentialSpawnRule.fixEntityId(name);
            EntityEntry ee = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
            Class<? extends Entity> clazz = ee == null ? null : ee.getEntityClass();
            if(clazz != null) checks.add((event, query) -> clazz.equals(query.getEntity(event).getClass()));
            else InControl.setup.getLogger().log(Level.ERROR, "Unknown mob '{}'!", name);
        }
        else {
            Set<Class<? extends Entity>> classes = new HashSet<>();
            for(String name : mobs) {
                String id = PotentialSpawnRule.fixEntityId(name);
                EntityEntry ee = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
                Class<? extends Entity> clazz = ee == null ? null : ee.getEntityClass();
                if(clazz != null) classes.add(clazz);
                else InControl.setup.getLogger().log(Level.ERROR, "Unknown mob '{}'!", name);
            }
            if(!classes.isEmpty()) checks.add((event, query) -> classes.contains(query.getEntity(event).getClass()));
        }
    }
    
    private void addExplosionCheck(AttributeMap map) {
        boolean explosion = map.get(EXPLOSION);
        if(explosion) checks.add((event, query) -> query.getSource(event) != null && query.getSource(event).isExplosion());
        else checks.add((event, query) -> query.getSource(event) == null || !query.getSource(event).isExplosion());
    }
    
    private void addProjectileCheck(AttributeMap map) {
        boolean projectile = map.get(PROJECTILE);
        if(projectile) checks.add((event, query) -> query.getSource(event) != null && query.getSource(event).isProjectile());
        else checks.add((event, query) -> query.getSource(event) == null || !query.getSource(event).isProjectile());
    }
    
    private void addFireCheck(AttributeMap map) {
        boolean fire = map.get(FIRE);
        if(fire) checks.add((event, query) -> query.getSource(event) != null && query.getSource(event).isFireDamage());
        else checks.add((event, query) -> query.getSource(event) == null || !query.getSource(event).isFireDamage());
    }
    
    private void addMagicCheck(AttributeMap map) {
        boolean magic = map.get(MAGIC);
        if(magic) checks.add((event, query) -> query.getSource(event) != null && query.getSource(event).isMagicDamage());
        else checks.add((event, query) -> query.getSource(event) == null || !query.getSource(event).isMagicDamage());
    }
    
    private void addRandomCheck(AttributeMap map) {
        final float r = map.get(RANDOM);
        checks.add((event,query) -> rnd.nextFloat() < r);
    }
    
    private void addDimensionCheck(AttributeMap map) {
        List<Integer> dimensions = map.getList(DIMENSION);
        if(dimensions.size() == 1) {
            Integer dim = dimensions.get(0);
            checks.add((event,query) -> query.getWorld(event).provider.getDimension() == dim);
        }
        else {
            Set<Integer> dims = new HashSet<>(dimensions);
            checks.add((event,query) -> dims.contains(query.getWorld(event).provider.getDimension()));
        }
    }
    
    private void addMinTimeCheck(AttributeMap map) {
        final int mintime = map.get(MINTIME);
        checks.add((event,query) -> {
            int time = (int) query.getWorld(event).getWorldTime();
            return (time % 24000) >= mintime;
        });
    }
    
    private void addMaxTimeCheck(AttributeMap map) {
        final int maxtime = map.get(MAXTIME);
        checks.add((event,query) -> {
            int time = (int) query.getWorld(event).getWorldTime();
            return (time % 24000) <= maxtime;
        });
    }
    
    private void addMinHeightCheck(AttributeMap map) {
        final int minheight = map.get(MINHEIGHT);
        checks.add((event,query) -> query.getY(event) >= minheight);
    }
    
    private void addMaxHeightCheck(AttributeMap map) {
        final int maxheight = map.get(MAXHEIGHT);
        checks.add((event,query) -> query.getY(event) <= maxheight);
    }
    
    private void addWeatherCheck(AttributeMap map) {
        String weather = map.get(WEATHER);
        boolean raining = weather.toLowerCase().startsWith("rain");
        boolean thunder = weather.toLowerCase().startsWith("thunder");
        if(raining) checks.add((event,query) -> query.getWorld(event).isRaining());
        else if(thunder) checks.add((event,query) -> query.getWorld(event).isThundering());
        else logger.log(Level.ERROR, "Unknown weather '{}'! Use 'rain' or 'thunder'", weather);
    }
    
    private void addTempCategoryCheck(AttributeMap map) {
        String tempcategory = map.get(TEMPCATEGORY).toLowerCase();
        Biome.TempCategory cat;
		switch(tempcategory) {
			case "cold": cat = Biome.TempCategory.COLD; break;
			case "medium": cat = Biome.TempCategory.MEDIUM; break;
			case "warm": cat = Biome.TempCategory.WARM; break;
			case "ocean": cat = Biome.TempCategory.OCEAN; break;
			default: logger.log(Level.ERROR, "Unknown tempcategory '{}'! Use one of 'cold', 'medium', 'warm',  or 'ocean'", tempcategory); return;
		}
        Biome.TempCategory finalCat = cat;
        checks.add((event,query) -> {
            Biome biome = query.getWorld(event).getBiome(query.getPos(event));
            return biome.getTempCategory() == finalCat;
        });
    }
    
    private void addDifficultyCheck(AttributeMap map) {
        String difficulty = map.get(DIFFICULTY).toLowerCase();
        EnumDifficulty diff = null;
        for(EnumDifficulty d : EnumDifficulty.values()) {
            if(d.getTranslationKey().endsWith("." + difficulty)) {
                diff = d;
                break;
            }
        }
        if(diff != null) {
            EnumDifficulty finalDiff = diff;
            checks.add((event,query) -> query.getWorld(event).getDifficulty() == finalDiff);
        }
        else logger.log(Level.ERROR, "Unknown difficulty '{}'! Use one of 'easy', 'normal', 'hard',  or 'peaceful'", difficulty);
    }
    
    private void addSourceCheck(AttributeMap map) {
        List<String> sources = map.getList(SOURCE);
        Set<String> sourceSet = new HashSet<>(sources);
        checks.add((event, query) -> {
            if(query.getSource(event) == null) return false;
            return sourceSet.contains(query.getSource(event).getDamageType());
        });
    }
    
    private void addMinSpawnDistCheck(AttributeMap map) {
        final float d = map.get(MINSPAWNDIST) * map.get(MINSPAWNDIST);
        checks.add((event,query) -> query.getPos(event).distanceSq(query.getWorld(event).getSpawnPoint()) >= d);
    }
    
    private void addMaxSpawnDistCheck(AttributeMap map) {
        final float d = map.get(MAXSPAWNDIST) * map.get(MAXSPAWNDIST);
        checks.add((event,query) -> query.getPos(event).distanceSq(query.getWorld(event).getSpawnPoint()) <= d);
    }
    
    private void addMinPlayerDistCheck(AttributeMap map) {
        final float d = map.get(MINPLAYERDIST);
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            return !query.getWorld(event).isAnyPlayerWithinRangeAt(pos.getX(), pos.getY(), pos.getZ(), d);
        });
    }
    
    private void addMaxPlayerDistCheck(AttributeMap map) {
        final float d = map.get(MAXPLAYERDIST);
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            return query.getWorld(event).isAnyPlayerWithinRangeAt(pos.getX(), pos.getY(), pos.getZ(), d);
        });
    }
    
    private void addMinLightCheck(AttributeMap map) {
        final int minlight = map.get(MINLIGHT);
        checks.add((event,query) -> query.getWorld(event).getLight(query.getPos(event), true) >= minlight);
    }
    
    private void addMaxLightCheck(AttributeMap map) {
        final int maxlight = map.get(MAXLIGHT);
        checks.add((event,query) -> query.getWorld(event).getLight(query.getPos(event), true) <= maxlight);
    }
    
    private void addMinAdditionalDifficultyCheck(AttributeMap map) {
        final float mindifficulty = map.get(MINDIFFICULTY);
        checks.add((event,query) -> query.getWorld(event).getDifficultyForLocation(query.getPos(event)).getAdditionalDifficulty() >= mindifficulty);
    }
    
    private void addMaxAdditionalDifficultyCheck(AttributeMap map) {
        final float maxdifficulty = map.get(MAXDIFFICULTY);
        checks.add((event,query) -> query.getWorld(event).getDifficultyForLocation(query.getPos(event)).getAdditionalDifficulty() <= maxdifficulty);
    }
    
    private void addSeeSkyCheck(AttributeMap map) {
        if(map.get(SEESKY)) checks.add((event,query) -> query.getWorld(event).canBlockSeeSky(query.getPos(event)));
        else checks.add((event,query) -> !query.getWorld(event).canBlockSeeSky(query.getPos(event)));
    }
    
    private void addBlocksCheck(AttributeMap map) {
        BiFunction<T, IEventQuery<T>, BlockPos> posFunction;
        if(map.has(BLOCKOFFSET)) posFunction = parseOffset(map.get(BLOCKOFFSET));
        else posFunction = (event, query) -> query.getValidBlockPos(event);
        
        List<String> blocks = map.getList(BLOCK);
        if(blocks.size() == 1) {
            String json = blocks.get(0);
            BiPredicate<World, BlockPos> blockMatcher = parseBlock(json, logger);
            if(blockMatcher != null) {
                checks.add((event, query) -> {
                    BlockPos pos = posFunction.apply(event, query);
                    return pos != null && blockMatcher.test(query.getWorld(event), pos);
                });
            }
            else logger.log(Level.ERROR, "Invalid block matcher '{}'!", json);
        }
        else {
            List<BiPredicate<World, BlockPos>> blockMatchers = new ArrayList<>();
            for(String block : blocks) {
                BiPredicate<World, BlockPos> blockMatcher = parseBlock(block, logger);
                if(blockMatcher == null) {
                    logger.log(Level.ERROR, "Invalid block matcher '{}'!", block);
                    return;
                }
                blockMatchers.add(blockMatcher);
            }
            checks.add((event,query) -> {
                BlockPos pos = posFunction.apply(event, query);
                if(pos != null) {
                    World world = query.getWorld(event);
                    for(BiPredicate<World, BlockPos> matcher : blockMatchers) {
                        if(matcher.test(world, pos)) return true;
                    }
                }
                return false;
            });
        }
    }
    
    private void addBiomesCheck(AttributeMap map) {
        List<String> biomes = map.getList(BIOME);
        if(biomes.size() == 1) {
            String biomename = biomes.get(0);
            checks.add((event,query) -> biomename.equals(compatibility.getBiomeName(query.getWorld(event).getBiome(query.getPos(event)))));
        }
        else {
            Set<String> biomenames = new HashSet<>(biomes);
            checks.add((event,query) -> biomenames.contains(compatibility.getBiomeName(query.getWorld(event).getBiome(query.getPos(event)))));
        }
    }
    
    private void addBiomeRegCheck(AttributeMap map) {
        List<String> regs = map.getList(BIOME_REG);
        if(regs.size() == 1) {
            ResourceLocation biomeLoc = new ResourceLocation(regs.get(0));
            checks.add((event, query) -> biomeLoc.equals(query.getWorld(event).getBiome(query.getPos(event)).getRegistryName()));
        }
        else {
            Set<ResourceLocation> biomeLocs = regs.stream().map(ResourceLocation::new).collect(Collectors.toCollection(HashSet::new));
            checks.add((event, query) -> biomeLocs.contains(query.getWorld(event).getBiome(query.getPos(event)).getRegistryName()));
        }
    }
    
    private void addBiomeTypesCheck(AttributeMap map) {
        List<String> biomeTypes = map.getList(BIOMETYPE);
        if(biomeTypes.size() == 1) {
            BiomeDictionary.Type type = BiomeDictionary.Type.getType(biomeTypes.get(0));
            checks.add((event,query) -> BiomeDictionary.getTypes(query.getWorld(event).getBiome(query.getPos(event))).contains(type));
        }
        else {
            Set<BiomeDictionary.Type> types = new HashSet<>();
            for(String s : biomeTypes) {
                types.add(BiomeDictionary.Type.getType(s));
            }
            checks.add((event,query) -> BiomeDictionary.getTypes(query.getWorld(event).getBiome(query.getPos(event))).stream().anyMatch(types::contains));
        }
    }
    
    public void addHelmetCheck(AttributeMap map) {
        List<Predicate<ItemStack>> items = getItems(map.getList(HELMET), logger);
        addArmorCheck(items, EntityEquipmentSlot.HEAD);
    }
    
    public void addChestplateCheck(AttributeMap map) {
        List<Predicate<ItemStack>> items = getItems(map.getList(CHESTPLATE), logger);
        addArmorCheck(items, EntityEquipmentSlot.CHEST);
    }
    
    public void addLeggingsCheck(AttributeMap map) {
        List<Predicate<ItemStack>> items = getItems(map.getList(LEGGINGS), logger);
        addArmorCheck(items, EntityEquipmentSlot.LEGS);
    }
    
    public void addBootsCheck(AttributeMap map) {
        List<Predicate<ItemStack>> items = getItems(map.getList(BOOTS), logger);
        addArmorCheck(items, EntityEquipmentSlot.FEET);
    }
    
    private void addArmorCheck(List<Predicate<ItemStack>> items, EntityEquipmentSlot slot) {
        checks.add((event,query) -> {
            EntityPlayer player = query.getPlayer(event);
            if(player != null) {
                ItemStack armorItem = player.getItemStackFromSlot(slot);
                if(!armorItem.isEmpty()) {
                    for(Predicate<ItemStack> item : items) {
                        if(item.test(armorItem)) return true;
                    }
                }
            }
            return false;
        });
    }
    
    public void addHeldItemCheck(AttributeMap map, Key<String> key) {
        List<Predicate<ItemStack>> items = getItems(map.getList(key), logger);
        checks.add((event,query) -> {
            EntityPlayer player = query.getPlayer(event);
            if(player != null) {
                ItemStack mainhand = player.getHeldItemMainhand();
                if(!mainhand.isEmpty()) {
                    for(Predicate<ItemStack> item : items) {
                        if(item.test(mainhand)) return true;
                    }
                }
            }
            return false;
        });
    }
    
    public void addOffHandItemCheck(AttributeMap map) {
        List<Predicate<ItemStack>> items = getItems(map.getList(OFFHANDITEM), logger);
        checks.add((event,query) -> {
            EntityPlayer player = query.getPlayer(event);
            if(player != null) {
                ItemStack offhand = player.getHeldItemOffhand();
                if(!offhand.isEmpty()) {
                    for(Predicate<ItemStack> item : items) {
                        if(item.test(offhand)) return true;
                    }
                }
            }
            return false;
        });
    }
    
    public void addBothHandsItemCheck(AttributeMap map) {
        List<Predicate<ItemStack>> items = getItems(map.getList(BOTHHANDSITEM), logger);
        checks.add((event,query) -> {
            EntityPlayer player = query.getPlayer(event);
            if(player != null) {
                ItemStack offhand = player.getHeldItemOffhand();
                if(!offhand.isEmpty()) {
                    for(Predicate<ItemStack> item : items) {
                        if(item.test(offhand)) return true;
                    }
                }
                ItemStack mainhand = player.getHeldItemMainhand();
                if(!mainhand.isEmpty()) {
                    for(Predicate<ItemStack> item : items) {
                        if(item.test(mainhand)) return true;
                    }
                }
            }
            return false;
        });
    }
    
    private void addStructureCheck(AttributeMap map) {
        String structure = map.get(STRUCTURE);
        checks.add((event,query) -> StructureCache.CACHE.isInStructure(query.getWorld(event), structure, query.getPos(event)));
    }
    
    private void addCanSpawnHereCheck(AttributeMap map) {
        boolean c = map.get(CANSPAWNHERE);
        if(c) {
            checks.add((event, query) -> {
                Entity entity = query.getEntity(event);
                if(entity instanceof EntityLiving) return ((EntityLiving)entity).getCanSpawnHere();
                else return false;
            });
        }
        else {
            checks.add((event, query) -> {
                Entity entity = query.getEntity(event);
                if(entity instanceof EntityLiving) return !((EntityLiving) entity).getCanSpawnHere();
                else return true;
            });
        }
    }
    
    private void addNotCollidingCheck(AttributeMap map) {
        boolean c = map.get(NOTCOLLIDING);
        if(c) {
            checks.add((event, query) -> {
                Entity entity = query.getEntity(event);
                if(entity instanceof EntityLiving) return ((EntityLiving) entity).isNotColliding();
                else return false;
            });
        }
        else {
            checks.add((event, query) -> {
                Entity entity = query.getEntity(event);
                if(entity instanceof EntityLiving) return !((EntityLiving) entity).isNotColliding();
                else return true;
            });
        }
    }
    
    private void addRealPlayerCheck(AttributeMap map) {
        boolean asPlayer = map.get(REALPLAYER);
        if(asPlayer) checks.add((event, query) -> query.getAttacker(event) != null && isRealPlayer(query.getAttacker(event)));
        else checks.add((event, query) -> query.getAttacker(event) == null || !isRealPlayer(query.getAttacker(event)));
    }
    
    private void addFakePlayerCheck(AttributeMap map) {
        boolean asPlayer = map.get(FAKEPLAYER);
        if(asPlayer) checks.add((event, query) -> query.getAttacker(event) != null && isFakePlayer(query.getAttacker(event)));
        else checks.add((event, query) -> query.getAttacker(event) == null || !isFakePlayer(query.getAttacker(event)));
    }
    
    private void addModsCheck(AttributeMap map) {
        List<String> mods = map.getList(MOD);
        if(mods.size() == 1) {
            String modid = mods.get(0);
            checks.add((event, query) -> modid.equals(InControl.instance.modCache.getMod(query.getEntity(event))));
        }
        else {
			Set<String> modids = new HashSet<>(mods);
            checks.add((event, query) -> modids.contains(InControl.instance.modCache.getMod(query.getEntity(event))));
        }
    }
    
    private void addMinCountCheck(AttributeMap map) {
        String json = map.get(MINCOUNT);
        CountInfo info = parseCountInfo(json);
        if(info == null) {
            logger.log(Level.ERROR, "Invalid min count entry '{}'!", json);
            return;
        }
        BiFunction<World, Entity, Integer> counter = getCounter(info);
        Function<World, Integer> amountAdjuster = getAmountAdjuster(info, info.amount);
        
        checks.add((event, query) -> {
            World world = query.getWorld(event);
            int count = counter.apply(world, query.getEntity(event));
            int amount = amountAdjuster.apply(world);
            return count >= amount;
        });
    }
    
    private void addMaxCountCheck(AttributeMap map) {
        String json = map.get(MAXCOUNT);
        CountInfo info = parseCountInfo(json);
        if(info == null) {
            logger.log(Level.ERROR, "Invalid max count entry '{}'!", json);
            return;
        }
        BiFunction<World, Entity, Integer> counter = getCounter(info);
        Function<World, Integer> amountAdjuster = getAmountAdjuster(info, info.amount);
        
        checks.add((event, query) -> {
            World world = query.getWorld(event);
            int count = counter.apply(world, query.getEntity(event));
            int amount = amountAdjuster.apply(world);
            return count < amount;
        });
    }
    
    private void addSummerCheck(AttributeMap map) {
        boolean s = map.get(SUMMER);
        checks.add((event, query) -> s == compatibility.isSummer(query.getWorld(event)));
    }
    
    private void addWinterCheck(AttributeMap map) {
        boolean s = map.get(WINTER);
        checks.add((event, query) -> s == compatibility.isWinter(query.getWorld(event)));
    }
    
    private void addSpringCheck(AttributeMap map) {
        boolean s = map.get(SPRING);
        checks.add((event, query) -> s == compatibility.isSpring(query.getWorld(event)));
    }
    
    private void addAutumnCheck(AttributeMap map) {
        boolean s = map.get(AUTUMN);
        checks.add((event, query) -> s == compatibility.isAutumn(query.getWorld(event)));
    }
    
    private void addGameStageCheck(AttributeMap map) {
        String stage = map.get(GAMESTAGE);
        checks.add((event, query) -> compatibility.hasGameStage(query.getPlayer(event), stage));
    }
    
    private void addHarvestMoonCheck(AttributeMap map) {
        boolean s = map.get(HARVEST_MOON);
        checks.add((event, query) -> s == compatibility.isHarvestMoonNyx(query.getWorld(event)));
    }
    
    private void addStarShowerCheck(AttributeMap map) {
        boolean s = map.get(STAR_SHOWER);
        if(compatibility.hasNyx()) checks.add((event, query) -> s == compatibility.isStarShowerNyx(query.getWorld(event)));
        else checks.add((event, query) -> s == compatibility.isStarShowerHyxcate(query.getWorld(event)));
    }
    
    private void addBloodMoonCheck(AttributeMap map) {
        boolean s = map.get(BLOOD_MOON);
        if(compatibility.hasNyx()) checks.add((event, query) -> s == compatibility.isBloodMoonNyx(query.getWorld(event)));
        else checks.add((event, query) -> s == compatibility.isBloodMoonHyxcate(query.getWorld(event)));
    }
    
    private void addFullMoonCheck(AttributeMap map) {
        boolean s = map.get(FULL_MOON);
        if(compatibility.hasNyx()) checks.add((event, query) -> s == compatibility.isFullMoonNyx(query.getWorld(event)));
        else checks.add((event, query) -> s == compatibility.isFullMoonHyxcate(query.getWorld(event)));
    }
    
    private void addRedGiantCheck(AttributeMap map) {
        boolean s = map.get(RED_GIANT);
        checks.add((event, query) -> s == compatibility.isRedGiantHyxcate(query.getWorld(event)));
    }
    
    private void addGrimEclipseCheck(AttributeMap map) {
        boolean s = map.get(GRIM_ECLIPSE);
        checks.add((event, query) -> s == compatibility.isGrimEclipseHyxcate(query.getWorld(event)));
    }
    
    private void addBlueMoonCheck(AttributeMap map) {
        boolean s = map.get(BLUE_MOON);
        checks.add((event, query) -> s == compatibility.isBlueMoonHyxcate(query.getWorld(event)));
    }
    
    public void addBaubleCheck(AttributeMap map, Key<String> key, Supplier<int[]> slotSupplier) {
        List<Predicate<ItemStack>> items = getItems(map.getList(key), logger);
        checks.add((event,query) -> {
            EntityPlayer player = query.getPlayer(event);
            if(player != null) {
                for(int slot : slotSupplier.get()) {
                    ItemStack stack = compatibility.getBaubleStack(player, slot);
                    if(!stack.isEmpty()) {
                        for(Predicate<ItemStack> item : items) {
                            if(item.test(stack)) return true;
                        }
                    }
                }
            }
            return false;
        });
    }
    
    private void addInCityCheck(AttributeMap map) {
        if(map.get(INCITY)) checks.add((event,query) -> compatibility.isCity(query, event));
        else checks.add((event,query) -> !compatibility.isCity(query, event));
    }
    
    private void addInStreetCheck(AttributeMap map) {
        if(map.get(INSTREET)) checks.add((event,query) -> compatibility.isStreet(query, event));
        else checks.add((event,query) -> !compatibility.isStreet(query, event));
    }
    
    private void addInSphereCheck(AttributeMap map) {
        if(map.get(INSPHERE)) checks.add((event,query) -> compatibility.inSphere(query, event));
        else checks.add((event,query) -> !compatibility.inSphere(query, event));
    }
    
    private void addInBuildingCheck(AttributeMap map) {
        if(map.get(INBUILDING)) checks.add((event,query) -> compatibility.isBuilding(query, event));
        else checks.add((event,query) -> !compatibility.isBuilding(query, event));
    }
    
    public boolean match(T event, IEventQuery<T> query) {
        for(BiFunction<T, IEventQuery<T>, Boolean> rule : checks) {
            if(!rule.apply(event, query)) return false;
        }
        return true;
    }
    
    
    
    @Nonnull
    private BiFunction<T, IEventQuery<T>, BlockPos> parseOffset(String json) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        JsonObject obj = element.getAsJsonObject();
        
        int offsetX;
        int offsetY;
        int offsetZ;
        
        if(obj.has("offset")) {
            JsonObject offset = obj.getAsJsonObject("offset");
            offsetX = offset.has("x") ? offset.get("x").getAsInt() : 0;
            offsetY = offset.has("y") ? offset.get("y").getAsInt() : 0;
            offsetZ = offset.has("z") ? offset.get("z").getAsInt() : 0;
        }
        else {
            offsetX = 0;
            offsetY = 0;
            offsetZ = 0;
        }
        
        if(obj.has("look")) {
            return (event, query) -> {
                RayTraceResult result = LookAtTools.getMovingObjectPositionFromPlayer(query.getWorld(event), query.getPlayer(event), false);
                if(result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) return result.getBlockPos().add(offsetX, offsetY, offsetZ);
                else return query.getValidBlockPos(event).add(offsetX, offsetY, offsetZ);
            };
        }
        return (event, query) -> query.getValidBlockPos(event).add(offsetX, offsetY, offsetZ);
    }
    
    
    
    private static final int[] EMPTYINTS = new int[0];
    
    @Nullable
    private static BiPredicate<World, BlockPos> parseBlock(String json, Logger logger) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        if(element.isJsonPrimitive()) {
            String blockname = element.getAsString();
            if(blockname.startsWith("ore:")) {
                int oreId = OreDictionary.getOreID(blockname.substring(4));
                return (world, pos) -> isMatchingOreDict(oreId, world.getBlockState(pos).getBlock());
            }
            else {
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockname));
                if(block == null) {
                    logger.log(Level.ERROR, "Block '{}' is not valid!", blockname);
                    return null;
                }
                return (world, pos) -> world.getBlockState(pos).getBlock() == block;
            }
        }
        else if(element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            BiPredicate<World, BlockPos> test;
            if(obj.has("ore")) {
                int oreId = OreDictionary.getOreID(obj.get("ore").getAsString());
                test = (world, pos) -> isMatchingOreDict(oreId, world.getBlockState(pos).getBlock());
            }
            else if(obj.has("block")) {
                String blockname = obj.get("block").getAsString();
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockname));
                if(block == null) {
                    logger.log(Level.ERROR, "Block '{}' is not valid!", blockname);
                    return null;
                }
                if(obj.has("properties")) {
                    IBlockState blockState = block.getDefaultState();
                    JsonArray propArray = obj.get("properties").getAsJsonArray();
                    for(JsonElement el : propArray) {
                        JsonObject propObj = el.getAsJsonObject();
                        String name = propObj.get("name").getAsString();
                        String value = propObj.get("value").getAsString();
                        for(IProperty<?> key : blockState.getPropertyKeys()) {
                            if(name.equals(key.getName())) blockState = set(blockState, key, value);
                        }
                    }
                    IBlockState finalBlockState = blockState;
                    test = (world, pos) -> world.getBlockState(pos) == finalBlockState;
                }
                else test = (world, pos) -> world.getBlockState(pos).getBlock() == block;
            }
            else test = (world, pos) -> true;
            
            if(obj.has("mod")) {
                String mod = obj.get("mod").getAsString();
                BiPredicate<World, BlockPos> finalTest = test;
                test = (world, pos) -> finalTest.test(world, pos) && mod.equals(world.getBlockState(pos).getBlock().getRegistryName().getNamespace());
            }
            if(obj.has("energy")) {
                Predicate<Integer> energy = getExpression(obj.get("energy"), logger);
                if(energy != null) {
                    EnumFacing side;
                    if(obj.has("side")) side = EnumFacing.byName(obj.get("side").getAsString().toLowerCase());
                    else side = null;
                    BiPredicate<World, BlockPos> finalTest = test;
                    test = (world, pos) -> finalTest.test(world, pos) && energy.test(getEnergy(world, pos, side));
                }
            }
            if(obj.has("contains")) {
                EnumFacing side;
                if(obj.has("side")) side = EnumFacing.byName(obj.get("energyside").getAsString().toLowerCase());
                else side = null;
                List<Predicate<ItemStack>> items = getItems(obj.get("contains"), logger);
                BiPredicate<World, BlockPos> finalTest = test;
                test = (world, pos) -> finalTest.test(world, pos) && contains(world, pos, side, items);
            }
            return test;
        }
        else logger.log(Level.ERROR, "Block description '{}' is not valid!", json);
        return null;
    }
    
    private static List<Predicate<ItemStack>> getItems(JsonElement itemObj, Logger logger) {
        List<Predicate<ItemStack>> items = new ArrayList<>();
        if(itemObj.isJsonObject()) {
            Predicate<ItemStack> matcher = getMatcher(itemObj.getAsJsonObject(), logger);
            if(matcher != null) items.add(matcher);
        }
        else if(itemObj.isJsonArray()) {
            for(JsonElement element : itemObj.getAsJsonArray()) {
                JsonObject obj = element.getAsJsonObject();
                Predicate<ItemStack> matcher = getMatcher(obj, logger);
                if(matcher != null) items.add(matcher);
            }
        }
        else logger.log(Level.ERROR, "Item description is not valid!");
        return items;
    }
    
    private static boolean isMatchingOreDict(int oreId, Block block) {
        ItemStack stack = new ItemStack(block);
        int[] oreIDs = stack.isEmpty() ? EMPTYINTS : OreDictionary.getOreIDs(stack);
        return isMatchingOreId(oreIDs, oreId);
    }
    
    private static boolean isMatchingOreId(int[] oreIDs, int oreId) {
		for(int id : oreIDs) {
			if(id == oreId) return true;
		}
		return false;
    }
    
    public static List<Predicate<ItemStack>> getItems(List<String> itemNames, Logger logger) {
        List<Predicate<ItemStack>> items = new ArrayList<>();
        for(String json : itemNames) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(json);
            if(element.isJsonPrimitive()) {
                String name = element.getAsString();
                Predicate<ItemStack> matcher = getMatcher(name, logger);
                if(matcher != null) items.add(matcher);
            }
            else if(element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                Predicate<ItemStack> matcher = getMatcher(obj, logger);
                if(matcher != null) items.add(matcher);
            }
            else logger.log(Level.ERROR, "Item description '{}' is not valid!", json);
        }
        return items;
    }
    
    private static boolean contains(World world, BlockPos pos, @Nullable EnumFacing side, @Nonnull List<Predicate<ItemStack>> matchers) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity != null && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
            IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            for(int i = 0 ; i < handler.getSlots() ; i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if(!stack.isEmpty()) {
                    for(Predicate<ItemStack> matcher : matchers) {
                        if(matcher.test(stack)) return true;
                    }
                }
            }
        }
        return false;
    }
    
    public static <T extends Comparable<T>> IBlockState set(IBlockState state, IProperty<T> property, String value) {
        Optional<T> optionalValue = property.parseValue(value);
        if(optionalValue.isPresent()) return state.withProperty(property, optionalValue.get());
        else return state;
    }
    
    private static Predicate<Integer> getExpression(JsonElement element, Logger logger) {
        if(element.isJsonPrimitive()) {
            if(element.getAsJsonPrimitive().isNumber()) {
                int amount = element.getAsInt();
                return i -> i == amount;
            }
            else return getExpression(element.getAsString(), logger);
        }
        else {
            logger.log(Level.ERROR, "Bad expression!");
            return null;
        }
    }
    
    private static Predicate<Integer> getExpression(String expression, Logger logger) {
        try {
            if(expression.startsWith(">=")) {
                int amount = Integer.parseInt(expression.substring(2));
                return i -> i >= amount;
            }
            else if(expression.startsWith(">")) {
                int amount = Integer.parseInt(expression.substring(1));
                return i -> i > amount;
            }
            else if(expression.startsWith("<=")) {
                int amount = Integer.parseInt(expression.substring(2));
                return i -> i <= amount;
            }
            else if(expression.startsWith("<")) {
                int amount = Integer.parseInt(expression.substring(1));
                return i -> i < amount;
            }
            else if(expression.startsWith("=")) {
                int amount = Integer.parseInt(expression.substring(1));
                return i -> i == amount;
            }
            else if(expression.startsWith("!=") || expression.startsWith("<>")) {
                int amount = Integer.parseInt(expression.substring(2));
                return i -> i != amount;
            }
            else if(expression.contains("-")) {
                String[] split = StringUtils.split(expression, "-");
                int amount1 = Integer.parseInt(split[0]);
                int amount2 = Integer.parseInt(split[1]);
                return i -> i >= amount1 && i <= amount2;
            }
            else {
                int amount = Integer.parseInt(expression);
                return i -> i == amount;
            }
        }
        catch(NumberFormatException e) {
            logger.log(Level.ERROR, "Bad expression '{}'!", expression);
            return null;
        }
    }
    
    private static Predicate<ItemStack> getMatcher(String name, Logger logger) {
        ItemStack stack = Tools.parseStack(name, logger);
        if(!stack.isEmpty()) {
            // Stack matching
            if(name.contains("/") && name.contains("@")) return s -> ItemStack.areItemsEqual(s, stack) && ItemStack.areItemStackTagsEqual(s, stack);
            else if(name.contains("/")) return s -> ItemStack.areItemsEqualIgnoreDurability(s, stack) && ItemStack.areItemStackTagsEqual(s, stack);
            else if (name.contains("@")) return s -> ItemStack.areItemsEqual(s, stack);
            else return s -> s.getItem() == stack.getItem();
        }
        return null;
    }
    
    private static Predicate<ItemStack> getMatcher(JsonObject obj, Logger logger) {
        if(obj.has("empty")) {
            boolean empty = obj.get("empty").getAsBoolean();
            return s -> s.isEmpty() == empty;
        }
        
        String name = obj.get("item").getAsString();
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
        if(item == null) {
            logger.log(Level.ERROR, "Unknown item '{}'!", name);
            return null;
        }
        
        Predicate<ItemStack> test;
        if(obj.has("damage")) {
            Predicate<Integer> damage = getExpression(obj.get("damage"), logger);
            if(damage == null) return null;
            test = s -> s.getItem() == item && damage.test(s.getItemDamage());
        }
        else test = s -> s.getItem() == item;
        
        if(obj.has("count")) {
            Predicate<Integer> count = getExpression(obj.get("count"), logger);
            if(count != null) {
                Predicate<ItemStack> finalTest = test;
                test = s -> finalTest.test(s) && count.test(s.getCount());
            }
        }
        if(obj.has("ore")) {
            int oreId = OreDictionary.getOreID(obj.get("ore").getAsString());
            Predicate<ItemStack> finalTest = test;
            test = s -> finalTest.test(s) && isMatchingOreId(s.isEmpty() ? EMPTYINTS : OreDictionary.getOreIDs(s), oreId);
        }
        if(obj.has("mod")) {
            String mod = obj.get("mod").getAsString();
            Predicate<ItemStack> finalTest = test;
            test = s -> finalTest.test(s) && "mod".equals(s.getItem().getRegistryName().getNamespace());
        }
        if(obj.has("nbt")) {
            List<Predicate<NBTTagCompound>> nbtMatchers = getNbtMatchers(obj, logger);
            if(nbtMatchers != null) {
                Predicate<ItemStack> finalTest = test;
                test = s -> finalTest.test(s) && nbtMatchers.stream().allMatch(p -> p.test(s.getTagCompound()));
            }
        }
        if(obj.has("energy")) {
            Predicate<Integer> energy = getExpression(obj.get("energy"), logger);
            if(energy != null) {
                Predicate<ItemStack> finalTest = test;
                test = s -> finalTest.test(s) && energy.test(getEnergy(s));
            }
        }
        return test;
    }
    
    private static int getEnergy(ItemStack stack) {
        IEnergyStorage cap =  stack.getCapability(CapabilityEnergy.ENERGY, null);
        if(cap != null) return cap.getEnergyStored();
        return 0;
    }
    
    private static int getEnergy(World world, BlockPos pos, @Nullable EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity != null) {
            IEnergyStorage cap = tileEntity.getCapability(CapabilityEnergy.ENERGY, side);
            if(cap != null) return cap.getEnergyStored();
        }
        return 0;
    }
    
    private static List<Predicate<NBTTagCompound>> getNbtMatchers(JsonObject obj, Logger logger) {
        JsonArray nbtArray = obj.getAsJsonArray("nbt");
        return getNbtMatchers(nbtArray, logger);
    }
    
    private static List<Predicate<NBTTagCompound>> getNbtMatchers(JsonArray nbtArray, Logger logger) {
        List<Predicate<NBTTagCompound>> nbtMatchers = new ArrayList<>();
        for(JsonElement element : nbtArray) {
            JsonObject o = element.getAsJsonObject();
            String tag = o.get("tag").getAsString();
            if(o.has("contains")) {
                List<Predicate<NBTTagCompound>> subMatchers = getNbtMatchers(o.getAsJsonArray("contains"), logger);
                nbtMatchers.add(tagCompound -> {
                    if(tagCompound != null) {
                        NBTTagList list = tagCompound.getTagList(tag, Constants.NBT.TAG_COMPOUND);
                        for(NBTBase base : list) {
                            for(Predicate<NBTTagCompound> matcher : subMatchers) {
                                if(matcher.test((NBTTagCompound) base)) return true;
                            }
                        }
                    }
                    return false;
                });
            }
            else {
                Predicate<Integer> nbt = getExpression(o.get("value"), logger);
                if(nbt == null) return null;
                nbtMatchers.add(tagCompound -> nbt.test(tagCompound.getInteger(tag)));
            }
        }
        return nbtMatchers;
    }
    
    private static class CountInfo {
        private final List<Class<? extends Entity>> entityClass = new ArrayList<>();
        private int amount;
        private boolean scaledPerPlayer = false;
        private boolean scaledPerChunk = false;
        private boolean passive = false;
        private boolean hostile = false;
        private String mod = null;

        public CountInfo() { }

        public CountInfo setAmount(int amount) {
            this.amount = amount;
            return this;
        }

        public CountInfo addEntityClass(Class<? extends Entity> entityClass) {
            if(entityClass != null) this.entityClass.add(entityClass);
            return this;
        }

        public CountInfo setScaledPerPlayer(boolean scaledPerPlayer) {
            this.scaledPerPlayer = scaledPerPlayer;
            return this;
        }

        public CountInfo setScaledPerChunk(boolean scaledPerChunk) {
            this.scaledPerChunk = scaledPerChunk;
            return this;
        }

        public CountInfo setPassive(boolean passive) {
            this.passive = passive;
            return this;
        }

        public CountInfo setHostile(boolean hostile) {
            this.hostile = hostile;
            return this;
        }

        public CountInfo setMod(String mod) {
            this.mod = mod;
            return this;
        }

        public String validate() {
            if(scaledPerPlayer && scaledPerChunk) return "You cannot combine 'perchunk' and 'perplayer'!";
            if(mod != null && !entityClass.isEmpty()) return "You cannot combine 'mod' with 'mob'!";
            if(passive && hostile) return "Don't use passive and hostile at the same time!";
            if((passive || hostile) && !entityClass.isEmpty()) return "You cannot combine 'passive' or 'hostile' with 'mob'!";
            return null;
        }
    }

    @Nullable
    private CountInfo parseCountInfo(String json) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        if(element.isJsonPrimitive()) {
            if(element.getAsJsonPrimitive().isString()) {
                String[] splitted = StringUtils.split(element.getAsString(), ',');
                int amount;
                try {
                    amount = Integer.parseInt(splitted[0]);
                }
                catch(NumberFormatException e) {
                    InControl.setup.getLogger().log(Level.ERROR, "Bad amount for mincount '{}'!", splitted[0]);
                    return null;
                }
                Class<? extends Entity> entityClass = null;
                if(splitted.length > 1) {
                    entityClass = findEntity(splitted[1]);
                    if(entityClass == null) {
                        InControl.setup.getLogger().log(Level.ERROR, "Cannot find mob '{}'!", splitted[1]);
                        return null;
                    }
                }
                return new CountInfo().setAmount(amount).addEntityClass(entityClass);
            }
            else {
                int amount = element.getAsInt();
                return new CountInfo().setAmount(amount);
            }
        }
        else if(element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            int amount = obj.get("amount").getAsInt();
            CountInfo info = new CountInfo().setAmount(amount);
            if(obj.has("mob")) {
                if(obj.get("mob").isJsonPrimitive()) {
                    String entity = obj.get("mob").getAsString();
                    Class<? extends Entity> entityClass = findEntity(entity);
                    if (entityClass == null) return null;
                    info.addEntityClass(entityClass);
                }
                else if(obj.get("mob").isJsonArray()) {
                    JsonArray array = obj.get("mob").getAsJsonArray();
                    for(JsonElement el : array) {
                        String entity = el.getAsString();
                        Class<? extends Entity> entityClass = findEntity(entity);
                        if(entityClass == null) {
                            InControl.setup.getLogger().log(Level.ERROR, "Cannot find mob '{}'!", entity);
                            return null;
                        }
                        info.addEntityClass(entityClass);
                    }
                }
                else {
                    InControl.setup.getLogger().log(Level.ERROR, "Bad entity tag in count description!");
                    return null;
                }
            }
            if(obj.has("mod")) info.setMod(obj.get("mod").getAsString());
            if(obj.has("perplayer")) info.setScaledPerPlayer(obj.get("perplayer").getAsBoolean());
            if(obj.has("perchunk")) info.setScaledPerChunk(obj.get("perchunk").getAsBoolean());
            if(obj.has("passive")) info.setPassive(obj.get("passive").getAsBoolean());
            if(obj.has("hostile")) info.setHostile(obj.get("hostile").getAsBoolean());
            String error = info.validate();
            if(error != null) {
                InControl.setup.getLogger().log(Level.ERROR, error);
                return null;
            }
            return info;
        }
        else {
            InControl.setup.getLogger().log(Level.ERROR, "Count description '{}' is not valid!", json);
            return null;
        }
    }

    private static Class<? extends Entity> findEntity(String entity) {
        Class<? extends Entity> entityClass;
        String id = PotentialSpawnRule.fixEntityId(entity);
        EntityEntry ee = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
        entityClass = ee == null ? null : ee.getEntityClass();
        if(entityClass == null) {
            InControl.setup.getLogger().log(Level.ERROR, "Unknown mob '{}'!", entity);
            return null;
        }
        return entityClass;
    }
    
    private static Function<World, Integer> getAmountAdjuster(CountInfo info, int infoAmount) {
        Function<World, Integer> amountAdjuster;
        if(info.scaledPerChunk) amountAdjuster = world -> infoAmount * InControl.setup.cache.getValidSpawnChunks(world) / 289;
        else if(info.scaledPerPlayer) amountAdjuster = world -> infoAmount * InControl.setup.cache.getValidPlayers(world);
        else amountAdjuster = world -> infoAmount;
        return amountAdjuster;
    }

    private static BiFunction<World, Entity, Integer> getCounter(CountInfo info) {
        BiFunction<World, Entity, Integer> counter;
        if(info.mod != null) {
            if(info.hostile) counter = (world, entity) -> InControl.setup.cache.getCountPerModHostile(world, info.mod);
            else if(info.passive) counter = (world, entity) -> InControl.setup.cache.getCountPerModPassive(world, info.mod);
            else counter = (world, entity) -> InControl.setup.cache.getCountPerMod(world, info.mod);
        }
        else if(info.hostile) counter = (world, entity) -> InControl.setup.cache.getCountHostile(world);
        else if(info.passive) counter = (world, entity) -> InControl.setup.cache.getCountPassive(world);
        else {
            List<Class<? extends Entity>> infoEntityClass = info.entityClass;
            if(infoEntityClass.isEmpty()) counter = (world, entity) -> InControl.setup.cache.getCount(world, entity.getClass());
            else if(infoEntityClass.size() == 1) {
                counter = (world, entity) -> {
                    Class<? extends Entity> entityType = infoEntityClass.get(0);
                    return InControl.setup.cache.getCount(world, entityType);
                };
            }
            else {
                counter = (world, entity) -> {
                    int amount = 0;
                    for(Class<? extends Entity> cls : infoEntityClass) {
                        amount += InControl.setup.cache.getCount(world, cls);
                    }
                    return amount;
                };
            }
        }
        return counter;
    }
    
    private static boolean isFakePlayer(Entity entity) {
        if(!(entity instanceof EntityPlayer)) return false;
        if(entity instanceof FakePlayer) return true;

        // If this returns false it is still possible we have a fake player. Try to find the player in the list of online players
        PlayerList playerList = DimensionManager.getWorld(0).getMinecraftServer().getPlayerList();
        EntityPlayerMP playerByUUID = playerList.getPlayerByUUID(((EntityPlayer) entity).getGameProfile().getId());
        if(playerByUUID == null) {
            // The player isn't online. Then it can't be real
            return true;
        }

        // The player is in the list. But is it this player?
        return entity != playerByUUID;
    }

    private static boolean isRealPlayer(Entity entity) {
        if(!(entity instanceof EntityPlayer)) return false;
        return !isFakePlayer(entity);
    }
}