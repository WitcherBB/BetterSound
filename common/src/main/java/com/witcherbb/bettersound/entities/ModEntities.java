package com.witcherbb.bettersound.entities;

import com.witcherbb.bettersound.Constants;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Constants.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<FakeStoolEntity>> FAKE_STOOL_ENTITY =
            register("fake_stool", EntityType.Builder.of(FakeStoolEntity::new, MobCategory.MISC)
                    .sized(0, 0).noSummon());

    private static <T extends Entity> RegistrySupplier<EntityType<T>> register(String id, EntityType.Builder<T> builder) {
        return ENTITY_TYPES.register(id, () -> builder.build(id));
    }

    public static void register() {
        ENTITY_TYPES.register();
    }
}
