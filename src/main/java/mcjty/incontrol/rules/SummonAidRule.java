package mcjty.incontrol.rules;

import com.google.gson.JsonElement;
import mcjty.incontrol.InControl;
import mcjty.incontrol.compat.ModRuleCompatibilityLayer;
import mcjty.incontrol.rules.support.GenericRuleEvaluator;
import mcjty.tools.rules.IEventQuery;
import mcjty.tools.rules.IModRuleCompatibilityLayer;
import mcjty.tools.rules.RuleBase;
import mcjty.tools.typed.Attribute;
import mcjty.tools.typed.AttributeMap;
import mcjty.tools.typed.GenericAttributeMapFactory;
import mcjty.tools.typed.Key;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static mcjty.incontrol.rules.support.RuleKeys.*;

public class SummonAidRule extends RuleBase<SummonEventGetter> {

    public static final IEventQuery<ZombieEvent.SummonAidEvent> EVENT_QUERY = new IEventQuery<ZombieEvent.SummonAidEvent>() {
        @Override
        public World getWorld(ZombieEvent.SummonAidEvent o) {
            return o.getWorld();
        }

        @Override
        public BlockPos getPos(ZombieEvent.SummonAidEvent o) {
            return new BlockPos(o.getX(), o.getY(), o.getZ());
        }

        @Override
        public BlockPos getValidBlockPos(ZombieEvent.SummonAidEvent o) {
            return new BlockPos(o.getX(), o.getY() - 1, o.getZ());
        }

        @Override
        public int getY(ZombieEvent.SummonAidEvent o) {
            return o.getY();
        }

        @Override
        public Entity getEntity(ZombieEvent.SummonAidEvent o) {
            return o.getEntity();
        }

        @Override
        public DamageSource getSource(ZombieEvent.SummonAidEvent o) {
            return null;
        }

        @Override
        public Entity getAttacker(ZombieEvent.SummonAidEvent o) {
            return null;
        }

        @Override
        public EntityPlayer getPlayer(ZombieEvent.SummonAidEvent o) {
            return null;
        }

        @Override
        public ItemStack getItem(ZombieEvent.SummonAidEvent o) {
            return ItemStack.EMPTY;
        }
    };
    
    private static final GenericAttributeMapFactory FACTORY = new GenericAttributeMapFactory();

    static {
        FACTORY
                .attribute(Attribute.create(HOSTILE))
                .attribute(Attribute.create(PASSIVE))
                
                //spawner
                
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
                
                //gamestage
                
                .attribute(Attribute.create(HARVEST_MOON))
                .attribute(Attribute.create(STAR_SHOWER))
                .attribute(Attribute.create(BLOOD_MOON))
                .attribute(Attribute.create(FULL_MOON))
                .attribute(Attribute.create(RED_GIANT))
                .attribute(Attribute.create(GRIM_ECLIPSE))
                .attribute(Attribute.create(BLUE_MOON))
                
                //helmet
                //chestplate
                //leggings
                //boots
                //playerhelditem
                //helditem
                //offhanditem
                //bothhandsitem
                
                //amulet
                //ring
                //belt
                //trinket
                //head
                //body
                //charm
                
                .attribute(Attribute.create(STRUCTURE))
                
                .attribute(Attribute.create(INCITY))
                .attribute(Attribute.create(INSTREET))
                .attribute(Attribute.create(INSPHERE))
                .attribute(Attribute.create(INBUILDING))
                
                .attribute(Attribute.create(CANSPAWNHERE))
                .attribute(Attribute.create(NOTCOLLIDING))
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
                //.attribute(Attribute.create(ACTION_SIZEMULTIPLY))
                //.attribute(Attribute.create(ACTION_SIZEADD))
                .attribute(Attribute.create(ACTION_ANGRY))
                .attribute(Attribute.createMulti(ACTION_HELDITEM))
                .attribute(Attribute.createMulti(ACTION_HELDITEMOFFHAND))
                .attribute(Attribute.createMulti(ACTION_ARMORBOOTS))
                .attribute(Attribute.createMulti(ACTION_ARMORLEGS))
                .attribute(Attribute.createMulti(ACTION_ARMORCHEST))
                .attribute(Attribute.createMulti(ACTION_ARMORHELMET))
                .attribute(Attribute.createMulti(ACTION_POTION))
                .attribute(Attribute.create(ACTION_ENCHANTDIFFICULTY))
        ;
    }

    private final GenericRuleEvaluator<ZombieEvent.SummonAidEvent> ruleEvaluator;
    private Event.Result result;

    private SummonAidRule(AttributeMap map) {
        super(InControl.setup.getLogger());
        ruleEvaluator = new GenericRuleEvaluator<>(map);
        addActions(map, new ModRuleCompatibilityLayer());
    }

    public static SummonAidRule parse(JsonElement element) {
        if(element == null) return null;
        else {
            AttributeMap map = FACTORY.parse(element);
            return new SummonAidRule(map);
        }
    }

    @Override
    protected void addActions(AttributeMap map, IModRuleCompatibilityLayer layer) {
        super.addActions(map, layer);

        if(map.has(ACTION_RESULT)) {
            String br = map.get(ACTION_RESULT);
            if("default".equals(br) || br.startsWith("def")) this.result = Event.Result.DEFAULT;
            else if ("allow".equals(br) || "true".equals(br)) this.result = Event.Result.ALLOW;
            else this.result = Event.Result.DENY;
        }
        else this.result = Event.Result.DEFAULT;

        if(map.has(ACTION_HEALTHMULTIPLY) || map.has(ACTION_HEALTHADD)) addHealthAction(map);
        if(map.has(ACTION_SPEEDMULTIPLY) || map.has(ACTION_SPEEDADD)) addSpeedAction(map);
        if(map.has(ACTION_DAMAGEMULTIPLY) || map.has(ACTION_DAMAGEADD)) addDamageAction(map);
        if(map.has(ACTION_ANGRY)) addAngryAction(map);
        if(map.has(ACTION_HELDITEM)) addHeldItem(map);
        if(map.has(ACTION_HELDITEMOFFHAND)) addHeldItemOffhand(map);
        if(map.has(ACTION_ARMORBOOTS)) addArmorItem(map, ACTION_ARMORBOOTS, EntityEquipmentSlot.FEET);
        if(map.has(ACTION_ARMORLEGS)) addArmorItem(map, ACTION_ARMORLEGS, EntityEquipmentSlot.LEGS);
        if(map.has(ACTION_ARMORHELMET)) addArmorItem(map, ACTION_ARMORHELMET, EntityEquipmentSlot.HEAD);
        if(map.has(ACTION_ARMORCHEST)) addArmorItem(map, ACTION_ARMORCHEST, EntityEquipmentSlot.CHEST);
        if(map.has(ACTION_POTION)) addPotionsAction(map);
        if(map.has(ACTION_ENCHANTDIFFICULTY)) addEnchantDifficultyAction(map);
    }

    private void addPotionsAction(AttributeMap map) {
        List<PotionEffect> effects = new ArrayList<>();
        for(String p : map.getList(ACTION_POTION)) {
            String[] splitted = StringUtils.split(p, ',');
            if(splitted == null || splitted.length != 3) {
                InControl.setup.getLogger().log(Level.ERROR, "Bad potion specifier '{}'! Use <potion>,<duration>,<amplifier>", p);
                continue;
            }
            Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(splitted[0]));
            if(potion == null) {
                InControl.setup.getLogger().log(Level.ERROR, "Can't find potion '{}'!", p);
                continue;
            }
            int duration = 0;
            int amplifier = 0;
            try {
                duration = Integer.parseInt(splitted[1]);
                amplifier = Integer.parseInt(splitted[2]);
            }
            catch(NumberFormatException e) {
                InControl.setup.getLogger().log(Level.ERROR, "Bad duration or amplifier integer for '{}'!", p);
                continue;
            }
            effects.add(new PotionEffect(potion, duration, amplifier));
        }
        if(!effects.isEmpty()) {
            actions.add(event -> {
                EntityLivingBase living = event.getZombieHelper();
                for(PotionEffect effect : effects) {
                    PotionEffect neweffect = new PotionEffect(effect.getPotion(), effect.getDuration(), effect.getAmplifier());
                    living.addPotionEffect(neweffect);
                }
            });
        }
    }

    private void addArmorItem(AttributeMap map, Key<String> itemKey, EntityEquipmentSlot slot) {
        List<Pair<Float, ItemStack>> items = getItemsWeighted(map.getList(itemKey));
        if(items.isEmpty()) return;
        if(items.size() == 1) {
            Pair<Float, ItemStack> pair = items.get(0);
            actions.add(event -> {
                EntityZombie helper = event.getZombieHelper();
                helper.setItemStackToSlot(slot, pair.getRight().copy());
            });
        }
        else {
            final float total = getTotal(items);
            actions.add(event -> {
                ItemStack item = getRandomItem(items, total);
                EntityZombie helper = event.getZombieHelper();
                helper.setItemStackToSlot(slot, item.copy());
            });
        }
    }

    private void addHeldItem(AttributeMap map) {
        List<Pair<Float, ItemStack>> items = getItemsWeighted(map.getList(ACTION_HELDITEM));
        if(items.isEmpty()) return;
        if(items.size() == 1) {
            Pair<Float, ItemStack> pair = items.get(0);
            actions.add(event -> {
                EntityZombie helper = event.getZombieHelper();
                helper.setHeldItem(EnumHand.MAIN_HAND, pair.getRight().copy());
            });
        }
        else {
            final float total = getTotal(items);
            actions.add(event -> {
                ItemStack item = getRandomItem(items, total);
                EntityZombie helper = event.getZombieHelper();
                helper.setHeldItem(EnumHand.MAIN_HAND, item.copy());
            });
        }
    }
    
    private void addHeldItemOffhand(AttributeMap map) {
        List<Pair<Float, ItemStack>> items = getItemsWeighted(map.getList(ACTION_HELDITEMOFFHAND));
        if(items.isEmpty()) return;
        if(items.size() == 1) {
            Pair<Float, ItemStack> pair = items.get(0);
            actions.add(event -> {
                EntityZombie helper = event.getZombieHelper();
                helper.setHeldItem(EnumHand.OFF_HAND, pair.getRight().copy());
            });
        }
        else {
            final float total = getTotal(items);
            actions.add(event -> {
                ItemStack item = getRandomItem(items, total);
                EntityZombie helper = event.getZombieHelper();
                helper.setHeldItem(EnumHand.OFF_HAND, item.copy());
            });
        }
    }
    
    private void addEnchantDifficultyAction(AttributeMap map) {
        boolean ench = map.has(ACTION_ENCHANTDIFFICULTY) ? map.get(ACTION_ENCHANTDIFFICULTY) : false;
        if(ench) {
            actions.add(event -> {
                EntityLivingBase entityLiving = event.getZombieHelper();
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

    private void addAngryAction(AttributeMap map) {
        if(map.get(ACTION_ANGRY)) {
            actions.add(event -> {
                EntityZombie helper = event.getZombieHelper();
                EntityPlayer player = event.getWorld().getClosestPlayerToEntity(helper, 50);
                if(player != null) helper.setAttackTarget(player);
            });
        }
    }

    private void addHealthAction(AttributeMap map) {
        float m = map.has(ACTION_HEALTHMULTIPLY) ? map.get(ACTION_HEALTHMULTIPLY) : 1;
        float a = map.has(ACTION_HEALTHADD) ? map.get(ACTION_HEALTHADD) : 0;
        actions.add(event -> {
            EntityZombie helper = event.getZombieHelper();
            IAttributeInstance entityAttribute = helper.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
            if(entityAttribute != null) {
                double newMax = entityAttribute.getBaseValue() * m + a;
                entityAttribute.setBaseValue(newMax);
                helper.setHealth((float) newMax);
            }
        });
    }

    private void addSpeedAction(AttributeMap map) {
        float m = map.has(ACTION_SPEEDMULTIPLY) ? map.get(ACTION_SPEEDMULTIPLY) : 1;
        float a = map.has(ACTION_SPEEDADD) ? map.get(ACTION_SPEEDADD) : 0;
        actions.add(event -> {
            EntityZombie helper = event.getZombieHelper();
            IAttributeInstance entityAttribute = helper.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
            if(entityAttribute != null) {
                double newMax = entityAttribute.getBaseValue() * m + a;
                entityAttribute.setBaseValue(newMax);
            }
        });
    }

    /*
    private void addSizeActions(AttributeMap map) {
        InControl.setup.getLogger().log(Level.WARN, "Mob resizing not implemented yet!");
        float m = map.has(ACTION_SIZEMULTIPLY) ? map.get(ACTION_SIZEMULTIPLY) : 1;
        float a = map.has(ACTION_SIZEADD) ? map.get(ACTION_SIZEADD) : 0;
        actions.add(event -> {
            EntityZombie helper = event.getZombieHelper();
            entityLiving.setSize(entityLiving.width * m + a, entityLiving.height * m + a);
        });
    }
    */
    
    private void addDamageAction(AttributeMap map) {
        float m = map.has(ACTION_DAMAGEMULTIPLY) ? map.get(ACTION_DAMAGEMULTIPLY) : 1;
        float a = map.has(ACTION_DAMAGEADD) ? map.get(ACTION_DAMAGEADD) : 0;
        actions.add(event -> {
            EntityZombie helper = event.getZombieHelper();
            IAttributeInstance entityAttribute = helper.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
            if(entityAttribute != null) {
                double newMax = entityAttribute.getBaseValue() * m + a;
                entityAttribute.setBaseValue(newMax);
            }
        });
    }

    public boolean match(ZombieEvent.SummonAidEvent event) {
        return ruleEvaluator.match(event, EVENT_QUERY);
    }

    public void action(ZombieEvent.SummonAidEvent event) {
        SummonEventGetter getter = new SummonEventGetter() {
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
                return new BlockPos(event.getX(), event.getY(), event.getZ());
            }

            @Override
            public EntityZombie getZombieHelper() {
                EntityZombie helper = event.getCustomSummonedAid();
                if(helper == null) helper = new EntityZombie(event.getWorld());
                return helper;
            }
        };
        for(Consumer<SummonEventGetter> action : actions) {
            action.accept(getter);
        }
    }

    public Event.Result getResult() {
        return result;
    }
}