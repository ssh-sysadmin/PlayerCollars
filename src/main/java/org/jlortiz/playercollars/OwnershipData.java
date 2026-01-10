package org.jlortiz.playercollars;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class OwnershipData {

    public final static String ownerKey = PlayerCollarsMod.MOD_ID + "_owner";
    public final static String bondedKey = PlayerCollarsMod.MOD_ID + "_bonded";

    public static @Nullable Pair<UUID, String> getOwner(ItemStack is) {
        CompoundTag $$1 = is.getTagElement(ownerKey);
        if ($$1 == null || !$$1.contains("uuid") || !$$1.contains("name"))
            return null;

        String name = $$1.getString("name");
        if (name.isEmpty())
            return null;
        
        return new Pair<>($$1.getUUID("uuid"), name);
    }

    public static void setOwner(ItemStack is, @Nullable UUID uuid, @Nullable String name) {
        if (uuid == null || name == null) {
            is.removeTagKey(ownerKey);
            return;
        }
        CompoundTag $$1 = is.getOrCreateTagElement(ownerKey);
        $$1.putUUID("uuid", uuid);
        $$1.putString("name", name);
    }

    public static @Nullable Pair<UUID, String> getBonded(ItemStack is) {
        CompoundTag $$1 = is.getTagElement(bondedKey);
        if ($$1 == null || !$$1.contains("uuid") || !$$1.contains("name"))
            return null;
        
        String name = $$1.getString("name");
        if (name.isEmpty())
            return null;

        return new Pair<>($$1.getUUID("uuid"), name);
    }

    public static void setBonded(ItemStack is, @Nullable UUID uuid, @Nullable String name) {
        if (uuid == null || name == null) {
            is.removeTagKey(bondedKey);
            return;
        }
        CompoundTag $$1 = is.getOrCreateTagElement(bondedKey);
        $$1.putUUID("uuid", uuid);
        $$1.putString("name", name);
    }


}
