package mcjty.incontrol.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.InControl;
import mcjty.incontrol.compat.ModRuleCompatibilityLayer;
import mcjty.incontrol.config.GeneralConfiguration;
import mcjty.incontrol.rules.support.GenericRuleEvaluator;
import mcjty.tools.rules.IEventQuery;
import mcjty.tools.rules.IModRuleCompatibilityLayer;
import mcjty.tools.rules.RuleBase;
import mcjty.tools.typed.Attribute;
import mcjty.tools.typed.AttributeMap;
import mcjty.tools.typed.GenericAttributeMapFactory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static mcjty.incontrol.rules.support.RuleKeys.*;


public class SpawnRule extends RuleBase<RuleBase.EventGetter> {

    public static final IEventQuery<LivingSpawnEvent.CheckSpawn> EVENT_QUERY = new IEventQuery<LivingSpawnEvent.CheckSpawn>() {
        @Override
        public World getWorld(LivingSpawnEvent.CheckSpawn o) {
            return o.getWorld();
        }

        @Override
        public BlockPos getPos(LivingSpawnEvent.CheckSpawn o) {
            return new BlockPos(o.getX(), o.getY(), o.getZ());
        }

        @Override
        public BlockPos getValidBlockPos(LivingSpawnEvent.CheckSpawn o) {
            return new BlockPos(o.getX(), o.getY() - 1, o.getZ());
        }

        @Override
        public int getY(LivingSpawnEvent.CheckSpawn o) {
            return (int) o.getY();
        }

        @Override
        public Entity getEntity(LivingSpawnEvent.CheckSpawn o) {
            return o.getEntity();
        }

        @Override
        public DamageSource getSource(LivingSpawnEvent.CheckSpawn o) {
            return null;
        }

        @Override
        public Entity getAttacker(LivingSpawnEvent.CheckSpawn o) {
            return null;
        }

        @Override
        public EntityPlayer getPlayer(LivingSpawnEvent.CheckSpawn o) {
            return getClosestPlayer(o.getWorld(), new BlockPos(o.getX(), o.getY(), o.getZ()));
        }

        @Override
        public ItemStack getItem(LivingSpawnEvent.CheckSpawn o) {
            return ItemStack.EMPTY;
        }
    };
    
    public static final IEventQuery<EntityJoinWorldEvent> EVENT_QUERY_JOIN = new IEventQuery<EntityJoinWorldEvent>() {
        @Override
        public World getWorld(EntityJoinWorldEvent o) {
            return o.getWorld();
        }

        @Override
        public BlockPos getPos(EntityJoinWorldEvent o) {
            return o.getEntity().getPosition();
        }

        @Override
        public BlockPos getValidBlockPos(EntityJoinWorldEvent o) {
            return o.getEntity().getPosition().down();
        }

        @Override
        public int getY(EntityJoinWorldEvent o) {
            return o.getEntity().getPosition().getY();
        }

        @Override
        public Entity getEntity(EntityJoinWorldEvent o) {
            return o.getEntity();
        }

        @Override
        public DamageSource getSource(EntityJoinWorldEvent o) {
            return null;
        }

        @Override
        public Entity getAttacker(EntityJoinWorldEvent o) {
            return null;
        }

        @Override
        public EntityPlayer getPlayer(EntityJoinWorldEvent o) {
            return getClosestPlayer(o.getWorld(), o.getEntity().getPosition());
        }

        @Override
        public ItemStack getItem(EntityJoinWorldEvent o) {
            return ItemStack.EMPTY;
        }
    };
    
    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();
    private static final GenericAttributeMapFactory SOURCE_RES_FACTORY = new GenericAttributeMapFactory();

    private static EntityPlayer getClosestPlayer(World world, BlockPos pos) {
        return world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), GeneralConfiguration.MAX_PLAYER_DISTANCE, false);
    }

    static {
        FACTORY
                .attribute(Attribute.create(HOSTILE))
                .attribute(Attribute.create(PASSIVE))
                
                .attribute(Attribute.create(SPAWNER))
                
                //player
                .attribute(Attribute.createMulti(MOB))
                
                //explosion
                //projectile
                //fire
                //magic
                
                .attribute(Attribute.create(RANDOM))
                .attribute(Attribute.createMulti(DIMENSION))
                .attribute(Attribute.create(MINTIME))
                .attribute(Attribute.create(MAXTIME))
                
                .attribute(Attribute.create(MINHEIGHT))
                .attribute(Attribute.create(MAXHEIGHT))
                .attribute(Attribute.create(WEATHER))
                .attribute(Attribute.create(TEMPCATEGORY))
                .attribute(Attribute.create(DIFFICULTY))
                
                //source
                
                .attribute(Attribute.create(MINSPAWNDIST))
                .attribute(Attribute.create(MAXSPAWNDIST))
                .attribute(Attribute.create(MINPLAYERDIST))
                .attribute(Attribute.create(MAXPLAYERDIST))
                
                .attribute(Attribute.create(MINLIGHT))
                .attribute(Attribute.create(MAXLIGHT))
                
                .attribute(Attribute.create(MINDIFFICULTY))
                .attribute(Attribute.create(MAXDIFFICULTY))
                
                .attribute(Attribute.create(SEESKY))
                .attribute(Attribute.createMulti(BLOCK))
                .attribute(Attribute.createMulti(BIOME))
                .attribute(Attribute.createMulti(BIOME_REG))
                .attribute(Attribute.createMulti(BIOMETYPE))
                
                .attribute(Attribute.create(SUMMER))
                .attribute(Attribute.create(WINTER))
                .attribute(Attribute.create(SPRING))
                .attribute(Attribute.create(AUTUMN))
                
                .attribute(Attribute.create(GAMESTAGE))
                
                .attribute(Attribute.create(HARVEST_MOON))
                .attribute(Attribute.create(STAR_SHOWER))
                .attribute(Attribute.create(BLOOD_MOON))
                .attribute(Attribute.create(FULL_MOON))
                .attribute(Attribute.create(RED_GIANT))
                .attribute(Attribute.create(GRIM_ECLIPSE))
                .attribute(Attribute.create(BLUE_MOON))
                
                .attribute(Attribute.createMulti(HELMET))
                .attribute(Attribute.createMulti(CHESTPLATE))
                .attribute(Attribute.createMulti(LEGGINGS))
                .attribute(Attribute.createMulti(BOOTS))
                .attribute(Attribute.createMulti(PLAYER_HELDITEM))
                .attribute(Attribute.createMulti(OFFHANDITEM))
                .attribute(Attribute.createMulti(BOTHHANDSITEM))
                
                .attribute(Attribute.createMulti(AMULET))
                .attribute(Attribute.createMulti(RING))
                .attribute(Attribute.createMulti(BELT))
                .attribute(Attribute.createMulti(TRINKET))
                .attribute(Attribute.createMulti(HEAD))
                .attribute(Attribute.createMulti(BODY))
                .attribute(Attribute.createMulti(CHARM))
                
                .attribute(Attribute.create(STRUCTURE))
                
                .attribute(Attribute.create(INCITY))
                .attribute(Attribute.create(INSTREET))
                .attribute(Attribute.create(INSPHERE))
                .attribute(Attribute.create(INBUILDING))
                
                .attribute(Attribute.create(CANSPAWNHERE))
                .attribute(Attribute.create(NOTCOLLIDING))
                .attribute(Attribute.create(GAMERULE))
                //realplayer
                //fakeplayer
                .attribute(Attribute.createMulti(MOD))
                .attribute(Attribute.create(MINCOUNT))
                .attribute(Attribute.create(MAXCOUNT))
                
                .attribute(Attribute.create(BLOCKOFFSET))
                .attribute(Attribute.create(STATE))

                .attribute(Attribute.create(ACTION_RESULT))
                .attribute(Attribute.create(ACTION_MESSAGE))
                .attribute(Attribute.create(ACTION_HEALTHMULTIPLY))
                .attribute(Attribute.create(ACTION_HEALTHADD))
                .attribute(Attribute.create(ACTION_SPEEDMULTIPLY))
                .attribute(Attribute.create(ACTION_SPEEDADD))
                .attribute(Attribute.create(ACTION_DAMAGEMULTIPLY))
                .attribute(Attribute.create(ACTION_DAMAGEADD))
                .attribute(Attribute.create(ACTION_SIZEMULTIPLY))
                .attribute(Attribute.create(ACTION_SIZEADD))
                .attribute(Attribute.create(ACTION_FOLLOWRANGEMULTIPLY))
                .attribute(Attribute.create(ACTION_FOLLOWRANGEADD))
                .attribute(Attribute.create(ACTION_KNOCKBACKRESISTANCEMULTIPLY))
                .attribute(Attribute.create(ACTION_KNOCKBACKRESISTANCEADD))
                .attribute(Attribute.create(ACTION_FLYINGSPEEDMULTIPLY))
                .attribute(Attribute.create(ACTION_FLYINGSPEEDADD))
                .attribute(Attribute.create(ACTION_ARMORMULTIPLY))
                .attribute(Attribute.create(ACTION_ARMORADD))
                .attribute(Attribute.create(ACTION_ARMORTOUGHNESSMULTIPLY))
                .attribute(Attribute.create(ACTION_ARMORTOUGHNESSADD))
                .attribute(Attribute.create(ACTION_ANGRY))
                .attribute(Attribute.create(ACTION_MOBNBT))
                .attribute(Attribute.create(ACTION_CUSTOMNAME))
                .attribute(Attribute.createMulti(ACTION_HELDITEM))
                .attribute(Attribute.createMulti(ACTION_HELDITEMOFFHAND))
                .attribute(Attribute.createMulti(ACTION_ARMORBOOTS))
                .attribute(Attribute.createMulti(ACTION_ARMORLEGS))
                .attribute(Attribute.createMulti(ACTION_ARMORCHEST))
                .attribute(Attribute.createMulti(ACTION_ARMORHELMET))
                .attribute(Attribute.createMulti(ACTION_POTION))
                .attribute(Attribute.create(ACTION_ENCHANTDIFFICULTY))
        ;
        
        SOURCE_RES_FACTORY
                .attribute(Attribute.createMulti(SOURCE_NAME))
                .attribute(Attribute.create(SOURCE_MULT))
                .attribute(Attribute.create(SOURCE_ADD))
        ;
    }

    private final boolean onJoin;
    private final GenericRuleEvaluator ruleEvaluator;
    private Event.Result result = null;

    private SpawnRule(AttributeMap map, boolean onJoin) {
        super(InControl.setup.getLogger());
        this.onJoin = onJoin;
        ruleEvaluator = new GenericRuleEvaluator<>(map);
        addActions(map, new ModRuleCompatibilityLayer());
    }

    public static SpawnRule parse(JsonElement element) {
        if(element == null) return null;
        else {
            JsonObject jsonObject = element.getAsJsonObject();
            boolean onJoin = jsonObject.has("onjoin") && jsonObject.get("onjoin").getAsBoolean();
            
            AttributeMap map = FACTORY.parse(element);
            if(jsonObject.has("sourcemodifiers")) {
                JsonArray resistances = jsonObject.get("sourcemodifiers").getAsJsonArray();
                for(JsonElement res : resistances) {
                    AttributeMap resMap = SOURCE_RES_FACTORY.parse(res);
                    map.addList(ACTION_SOURCE_MODIFIERS, resMap);
                }
            }
            return new SpawnRule(map, onJoin);
        }
    }

    @Override
    protected void addActions(AttributeMap map, IModRuleCompatibilityLayer layer) {
        super.addActions(map, layer);
        
        if(map.has(ACTION_FOLLOWRANGEMULTIPLY) || map.has(ACTION_FOLLOWRANGEADD)) addFollowRangeAction(map);
        if(map.has(ACTION_KNOCKBACKRESISTANCEMULTIPLY) || map.has(ACTION_KNOCKBACKRESISTANCEADD)) addKnockbackResistanceAction(map);
        if(map.has(ACTION_FLYINGSPEEDMULTIPLY) || map.has(ACTION_FLYINGSPEEDADD)) addFlyingSpeedAction(map);
        if(map.has(ACTION_ARMORMULTIPLY) || map.has(ACTION_ARMORADD)) addArmorAction(map);
        if(map.has(ACTION_ARMORTOUGHNESSMULTIPLY) || map.has(ACTION_ARMORTOUGHNESSADD)) addArmorToughnessAction(map);
        
        if(map.has(ACTION_SOURCE_MODIFIERS)) addSourceModifiers(map);
        
        if(map.has(ACTION_HELDITEMOFFHAND)) addHeldItemOffhand(map);
        if(map.has(ACTION_ENCHANTDIFFICULTY)) addEnchantDifficultyAction(map);
        
        if(map.has(ACTION_RESULT)) {
            String br = map.get(ACTION_RESULT);
            if("default".equals(br) || br.startsWith("def")) this.result = Event.Result.DEFAULT;
            else if("allow".equals(br) || "true".equals(br)) this.result = Event.Result.ALLOW;
            else this.result = Event.Result.DENY;
        }
        else this.result = null;
    }
    
    private void addFollowRangeAction(AttributeMap map) {
        float m = map.has(ACTION_FOLLOWRANGEMULTIPLY) ? map.get(ACTION_FOLLOWRANGEMULTIPLY) : 1;
        float a = map.has(ACTION_FOLLOWRANGEADD) ? map.get(ACTION_FOLLOWRANGEADD) : 0;
        actions.add(event -> {
            EntityLivingBase entityLiving = event.getEntityLiving();
            if(entityLiving != null) {
                IAttributeInstance entityAttribute = entityLiving.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
                if(entityAttribute != null) {
                    double newMax = entityAttribute.getBaseValue() * m + a;
                    entityAttribute.setBaseValue(newMax);
                }
            }
        });
    }
    
    private void addKnockbackResistanceAction(AttributeMap map) {
        float m = map.has(ACTION_KNOCKBACKRESISTANCEMULTIPLY) ? map.get(ACTION_KNOCKBACKRESISTANCEMULTIPLY) : 1;
        float a = map.has(ACTION_KNOCKBACKRESISTANCEADD) ? map.get(ACTION_KNOCKBACKRESISTANCEADD) : 0;
        actions.add(event -> {
            EntityLivingBase entityLiving = event.getEntityLiving();
            if(entityLiving != null) {
                IAttributeInstance entityAttribute = entityLiving.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
                if(entityAttribute != null) {
                    double newMax = entityAttribute.getBaseValue() * m + a;
                    entityAttribute.setBaseValue(newMax);
                }
            }
        });
    }
    
    private void addFlyingSpeedAction(AttributeMap map) {
        float m = map.has(ACTION_FLYINGSPEEDMULTIPLY) ? map.get(ACTION_FLYINGSPEEDMULTIPLY) : 1;
        float a = map.has(ACTION_FLYINGSPEEDADD) ? map.get(ACTION_FLYINGSPEEDADD) : 0;
        actions.add(event -> {
            EntityLivingBase entityLiving = event.getEntityLiving();
            if(entityLiving != null) {
                IAttributeInstance entityAttribute = entityLiving.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED);
                if(entityAttribute != null) {
                    double newMax = entityAttribute.getBaseValue() * m + a;
                    entityAttribute.setBaseValue(newMax);
                }
            }
        });
    }
    
    private void addArmorAction(AttributeMap map) {
        float m = map.has(ACTION_ARMORMULTIPLY) ? map.get(ACTION_ARMORMULTIPLY) : 1;
        float a = map.has(ACTION_ARMORADD) ? map.get(ACTION_ARMORADD) : 0;
        actions.add(event -> {
            EntityLivingBase entityLiving = event.getEntityLiving();
            if(entityLiving != null) {
                IAttributeInstance entityAttribute = entityLiving.getEntityAttribute(SharedMonsterAttributes.ARMOR);
                if(entityAttribute != null) {
                    double newMax = entityAttribute.getBaseValue() * m + a;
                    entityAttribute.setBaseValue(newMax);
                }
            }
        });
    }
    
    private void addArmorToughnessAction(AttributeMap map) {
        float m = map.has(ACTION_ARMORTOUGHNESSMULTIPLY) ? map.get(ACTION_ARMORTOUGHNESSMULTIPLY) : 1;
        float a = map.has(ACTION_ARMORTOUGHNESSADD) ? map.get(ACTION_ARMORTOUGHNESSADD) : 0;
        actions.add(event -> {
            EntityLivingBase entityLiving = event.getEntityLiving();
            if(entityLiving != null) {
                IAttributeInstance entityAttribute = entityLiving.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS);
                if(entityAttribute != null) {
                    double newMax = entityAttribute.getBaseValue() * m + a;
                    entityAttribute.setBaseValue(newMax);
                }
            }
        });
    }
    
    private void addSourceModifiers(AttributeMap map) {
        for(AttributeMap resMap : map.getList(ACTION_SOURCE_MODIFIERS)) {
            List<String> names = resMap.getList(SOURCE_NAME);
            float m = resMap.has(SOURCE_MULT) ? resMap.get(SOURCE_MULT) : 1;
            float a = resMap.has(SOURCE_ADD) ? resMap.get(SOURCE_ADD) : 0;
            
            for(String name : names) {
                NBTTagCompound resTag = new NBTTagCompound();
                resTag.setString("name", name);
                resTag.setFloat("mult", m);
                resTag.setFloat("add", a);
                actions.add(event -> {
                    EntityLivingBase entityLiving = event.getEntityLiving();
                    if(entityLiving != null) {
                        NBTTagCompound dataTag = entityLiving.getEntityData();
                        NBTTagList listTag = dataTag.getTagList("inctrl_sourcemod", 10);
                        listTag.appendTag(resTag);
                        dataTag.setTag("inctrl_sourcemod", listTag);
                    }
                });
            }
        }
    }
    
    private void addHeldItemOffhand(AttributeMap map) {
        final List<Pair<Float, ItemStack>> items = getItemsWeighted(map.getList(ACTION_HELDITEMOFFHAND));
        if(items.isEmpty()) return;
        if(items.size() == 1) {
            ItemStack item = items.get(0).getRight();
            actions.add(event -> {
                EntityLivingBase entityLiving = event.getEntityLiving();
                if(entityLiving != null) {
                    if(entityLiving instanceof EntityEnderman) {
                        if(item.getItem() instanceof ItemBlock) {
                            ItemBlock b = (ItemBlock)item.getItem();
                            ((EntityEnderman) entityLiving).setHeldBlockState(b.getBlock().getStateFromMeta(b.getMetadata(item.getItemDamage())));
                        }
                    }
                    else entityLiving.setHeldItem(EnumHand.OFF_HAND, item.copy());
                }
            });
        }
        else {
            final float total = getTotal(items);
            actions.add(event -> {
                EntityLivingBase entityLiving = event.getEntityLiving();
                if(entityLiving != null) {
                    ItemStack item = getRandomItem(items, total);
                    if(entityLiving instanceof EntityEnderman) {
                        if(item.getItem() instanceof ItemBlock) {
                            ItemBlock b = (ItemBlock) item.getItem();
                            ((EntityEnderman) entityLiving).setHeldBlockState(b.getBlock().getStateFromMeta(b.getMetadata(item.getItemDamage())));
                        }
                    }
                    else entityLiving.setHeldItem(EnumHand.OFF_HAND, item.copy());
                }
            });
        }
    }
    
    private void addEnchantDifficultyAction(AttributeMap map) {
        boolean ench = map.has(ACTION_ENCHANTDIFFICULTY) ? map.get(ACTION_ENCHANTDIFFICULTY) : false;
        if(ench) {
            actions.add(event -> {
                EntityLivingBase entityLiving = event.getEntityLiving();
                if(entityLiving != null) {
                    float f = entityLiving.world.getDifficultyForLocation(new BlockPos(entityLiving)).getClampedAdditionalDifficulty();
                    if(!entityLiving.getHeldItemMainhand().isEmpty() && entityLiving.getRNG().nextFloat() < 0.25F * f) {
                        entityLiving.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, EnchantmentHelper.addRandomEnchantment(entityLiving.getRNG(), entityLiving.getHeldItemMainhand(), (int)(5.0F + f * (float)entityLiving.getRNG().nextInt(18)), false));
                    }
                    if(!entityLiving.getHeldItemOffhand().isEmpty() && entityLiving.getRNG().nextFloat() < 0.25F * f) {
                        entityLiving.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, EnchantmentHelper.addRandomEnchantment(entityLiving.getRNG(), entityLiving.getHeldItemOffhand(), (int)(5.0F + f * (float)entityLiving.getRNG().nextInt(18)), false));
                    }
                    for(EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values()) {
                        if(entityequipmentslot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                            ItemStack itemstack = entityLiving.getItemStackFromSlot(entityequipmentslot);
                            if(!itemstack.isEmpty() && entityLiving.getRNG().nextFloat() < 0.5F * f) {
                                entityLiving.setItemStackToSlot(entityequipmentslot, EnchantmentHelper.addRandomEnchantment(entityLiving.getRNG(), itemstack, (int)(5.0F + f * (float)entityLiving.getRNG().nextInt(18)), false));
                            }
                        }
                    }
                }
            });
        }
    }

    public boolean match(LivingSpawnEvent.CheckSpawn event) {
        return ruleEvaluator.match(event, EVENT_QUERY);
    }

    public boolean match(EntityJoinWorldEvent event) {
        return ruleEvaluator.match(event, EVENT_QUERY_JOIN);
    }

    public void action(LivingSpawnEvent.CheckSpawn event) {
        EventGetter getter = new EventGetter() {
            @Override
            public EntityLivingBase getEntityLiving() {
                return event.getEntityLiving();
            }

            @Override
            public EntityPlayer getPlayer() {
                return null;
            }

            @Override
            public World getWorld() {
                return event.getWorld();
            }

            @Override
            public BlockPos getPosition() {
                return event.getEntityLiving().getPosition();
            }
        };
        for(Consumer<EventGetter> action : actions) {
            action.accept(getter);
        }
    }

    public void action(EntityJoinWorldEvent event) {
        EventGetter getter = new EventGetter() {
            @Override
            public EntityLivingBase getEntityLiving() {
                return event.getEntity() instanceof EntityLivingBase ? (EntityLivingBase) event.getEntity() : null;
            }

            @Override
            public EntityPlayer getPlayer() {
                return null;
            }

            @Override
            public World getWorld() {
                return event.getWorld();
            }

            @Override
            public BlockPos getPosition() {
                return event.getEntity() != null ? event.getEntity().getPosition() : null;
            }
        };
        for(Consumer<EventGetter> action : actions) {
            action.accept(getter);
        }
    }

    @Nullable
    public Event.Result getResult() {
        return result;
    }

    public boolean isOnJoin() {
        return onJoin;
    }
}