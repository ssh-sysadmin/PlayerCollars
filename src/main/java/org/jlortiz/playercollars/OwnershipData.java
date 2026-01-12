package org.jlortiz.playercollars;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class OwnershipData {

    public final static String ownersKey = PlayerCollarsMod.MOD_ID + "_owners";
    public final static String bondedKey = PlayerCollarsMod.MOD_ID + "_bonded";

    public static ArrayList<Pair<UUID, String>> getOwnersArrayList(ItemStack is) {
        ArrayList<Pair<UUID, String>> outputList = new ArrayList<Pair<UUID, String>>();

        CompoundTag rootTag = is.getTag();

        if (rootTag == null)
            return outputList;

        ListTag list = rootTag.getList(ownersKey, Tag.TAG_COMPOUND);
        if (list == null)
            return outputList;

        Iterator<Tag> it = list.iterator();
        while (it.hasNext()) {
            CompoundTag tag = (CompoundTag) it.next();

            String name = tag.getString("name");
            if (name == null)
                name = "";

            outputList.add(new Pair<>(tag.getUUID("uuid"), name));
        }

        return outputList;
    }
    
    public static int getOwnersCount(ItemStack is)
    {
        CompoundTag rootTag = is.getTag();

        if (rootTag == null)
            return 0;

        ListTag list = rootTag.getList(ownersKey, Tag.TAG_COMPOUND);

        return list.size();
    }
    
    public static boolean isOwner(ItemStack is, UUID uuid, @Nullable String name)
    {
        CompoundTag rootTag = is.getTag();

        if (rootTag == null)
            return false;

        ListTag list = rootTag.getList(ownersKey, Tag.TAG_COMPOUND);

        Iterator<Tag> it = list.iterator();
        while (it.hasNext()) {
            CompoundTag tag = (CompoundTag) it.next();

            if (uuid.equals(tag.getUUID("uuid"))) {
                if (name == null)
                    return true;

                if (name.equals(tag.getString("name")))
                    return true;
            }
        }

        return false;
    }
    
    public static boolean isOwner(ItemStack is, Player player) {
        return isOwner(is, player.getUUID(), null);
    }

    public static void addOwner(ItemStack is, UUID uuid, String name) {
        CompoundTag rootTag = is.getOrCreateTag();

        if (rootTag.get(ownersKey) == null)
            rootTag.put(ownersKey, new ListTag());

        ListTag list = rootTag.getList(ownersKey, Tag.TAG_COMPOUND);

        // check if owner already included
        Iterator<Tag> it = list.iterator();
        while (it.hasNext()) {
            CompoundTag tag = (CompoundTag) it.next();
            if (tag.getUUID("uuid").equals(uuid)) {
                if (tag.getString("name") != name)
                    tag.putString("name", name);
                return;
            }
        }

        CompoundTag newTag = new CompoundTag();
        newTag.putUUID("uuid", uuid);
        newTag.putString("name", name);

        list.add(newTag);
    }
    
    public static void removeOwner(ItemStack is, UUID uuid, @Nullable String name) {
        CompoundTag rootTag = is.getTag();

        if (rootTag == null)
            return;

        ListTag list = rootTag.getList(ownersKey, Tag.TAG_COMPOUND);

        int index = 0;
        Iterator<Tag> it = list.iterator();
        while (it.hasNext()) {
            CompoundTag tag = (CompoundTag) it.next();

            if (uuid.equals(tag.getUUID("uuid"))) {
                if (name == null) {
                    list.remove(index);
                    break;
                }

                if (name.equals(tag.getString("name"))) {
                    list.remove(index);
                    break;
                }
            }
            index++;
        }

        if(list.size() == 0)
            rootTag.remove(ownersKey);
    }
    
    public static void removeOwner(ItemStack is, Player player) {
        removeOwner(is, player.getUUID(), null);
    }

    @Nullable
    public static Pair<UUID, String> getBonded(ItemStack is) {
        CompoundTag $$1 = is.getTagElement(bondedKey);
        if ($$1 == null || !$$1.contains("uuid") || !$$1.contains("name"))
            return null;
        
        return new Pair<>($$1.getUUID("uuid"), $$1.getString("name"));
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
