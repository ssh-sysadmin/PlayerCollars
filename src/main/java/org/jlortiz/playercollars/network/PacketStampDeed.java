package org.jlortiz.playercollars.network;

import java.util.UUID;
import java.util.function.Supplier;

import org.jlortiz.playercollars.OwnershipData;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.DeedItem;
import com.mojang.datafixers.util.Pair;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketStampDeed {
    public PacketStampDeed() {
    }

    public PacketStampDeed(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        EquipmentSlot hand = EquipmentSlot.MAINHAND;
        ItemStack is = player.getItemBySlot(hand);
        if (is.isEmpty() || !(is.getItem() instanceof DeedItem))
        {
            hand = EquipmentSlot.OFFHAND;
            is = player.getItemBySlot(hand);

            if (is.isEmpty() || !(is.getItem() instanceof DeedItem))
                return;
        }

        Pair<UUID, String> ownerData = OwnershipData.getOwner(is);
        if (ownerData == null)
            return;

        Pair<UUID, String> bondedData = OwnershipData.getBonded(is);
        if(bondedData != null)
            return;

        is = new ItemStack(PlayerCollarsMod.DEED_OF_OWNERSHIP_STAMPED.get());
        OwnershipData.setOwner(is, ownerData.getFirst(), ownerData.getSecond());
        OwnershipData.setBonded(is, player.getUUID(), player.getName().getString());

        player.setItemSlot(hand, is);
    }
}
