package org.jlortiz.playercollars.item;

import org.jlortiz.playercollars.OwnershipData;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class StampedDeedItem extends Item{

    public StampedDeedItem() {
        super(new Item.Properties().stacksTo(1));
    }
    
    @Override
    public Component getName(ItemStack is) {
        if (OwnershipData.getOwnersCount(is) == 0)
            return Component.translatable("item.playercollars.stamped_deed_of_ownership.invalid");
        return Component.translatable("item.playercollars.stamped_deed_of_ownership", OwnershipData.getOwnersArrayList(is).get(0).getSecond());
    }

}
