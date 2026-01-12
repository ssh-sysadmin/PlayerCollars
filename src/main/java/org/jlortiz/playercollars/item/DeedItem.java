package org.jlortiz.playercollars.item;

import java.util.UUID;

import org.jlortiz.playercollars.OwnershipData;
import org.jlortiz.playercollars.client.screen.DeedItemScreen;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.*;

public class DeedItem extends Item{

    public DeedItem() {
        super(new Item.Properties().stacksTo(1));
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack is = player.getItemInHand(hand);

        if (level.isClientSide()) {
            Pair<UUID, String> bonded = OwnershipData.getBonded(is);
            if (OwnershipData.getOwnersCount(is) == 1 && bonded == null) {
                if (OwnershipData.isOwner(is, player)) {
                    player.displayClientMessage(
                            Component.translatable("item.playercollars.deed_of_ownership.no_self_own"), true);
                    return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, is);
                }
                openTheScreen(is, player);
                return new InteractionResultHolder<ItemStack>(InteractionResult.CONSUME, is);
            }
        }
        else if (OwnershipData.getOwnersCount(is) == 0)
        {
            OwnershipData.addOwner(is, player.getUUID(), player.getName().getString());
            player.displayClientMessage(Component.translatable("item.playercollars.deed_of_ownership.filled_out"),
                    true);
            return new InteractionResultHolder<ItemStack>(InteractionResult.CONSUME, is);
        }

        return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, is);
    }
    
    @Override
    public Component getName(ItemStack is) {
        if(is != null)
            return Component.translatable("item.playercollars.deed_of_ownership.filled");
        return super.getName(is);
    }
    
    @OnlyIn(Dist.CLIENT)
    private void openTheScreen(ItemStack is, Player player) {
        Minecraft.getInstance().setScreen(new DeedItemScreen(is, player));
    }
}
