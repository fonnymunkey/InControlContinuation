package mcjty.incontrol.rules;

import mcjty.tools.varia.Tools;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class EntityModCache {

    private Map<Class, String> cache = new HashMap<>();

    public String getMod(Entity entity) {
        Class<? extends Entity> cls = entity.getClass();
        if (!cache.containsKey(cls)) {
            ResourceLocation loc = EntityList.getKey(cls);
            cache.put(cls, loc == null ? "$not_found$" : loc.getNamespace());
        }
        return cache.get(cls);
    }
}
