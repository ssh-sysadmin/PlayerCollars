package org.jlortiz.playercollars.item;

import java.util.UUID;

import org.jlortiz.playercollars.OwnershipData;

import com.mojang.datafixers.util.Pair;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class StampedDeedItem extends Item{

    public StampedDeedItem() {
        super(new Item.Properties().stacksTo(1));
    }
    
    @Override
    public Component getName(ItemStack is) {
        Pair<UUID, String> ownerData = OwnershipData.getOwner(is);
        if (ownerData == null)
            return Component.translatable("item.playercollars.stamped_deed_of_ownership.invalid");
        return Component.translatable("item.playercollars.stamped_deed_of_ownership", ownerData.getSecond());
    }

}
