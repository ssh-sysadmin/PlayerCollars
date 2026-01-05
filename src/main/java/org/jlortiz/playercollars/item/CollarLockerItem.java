package org.jlortiz.playercollars.item;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jlortiz.playercollars.PlayerCollarsMod;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import top.theillusivec4.curios.api.CuriosApi;

public class CollarLockerItem extends Item {
    public CollarLockerItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack is, Player player, LivingEntity otherEntity,
            InteractionHand hand) {

        if(!(otherEntity instanceof Player))
            return InteractionResult.PASS;

        AtomicBoolean found = new AtomicBoolean(false);
        CuriosApi.getCuriosInventory((Player) otherEntity)
                .ifPresent((handler) -> handler.getStacksHandler("necklace").ifPresent((slot) -> {

                    ItemStack isCollar = PlayerCollarsMod.filterStacksByOwner(slot.getStacks(), player.getUUID());
                    if (isCollar == null) {
                        isCollar = PlayerCollarsMod.filterStacksByOwner(slot.getCosmeticStacks(), player.getUUID());
                    }

                    if (isCollar == null) {
                        return;
                    }

                    found.set(true);
                    boolean hasBinding = PlayerCollarsMod.COLLAR_LOCKER_ITEM.get().getEnchantmentLevel(isCollar,
                            Enchantments.BINDING_CURSE) > 0;
                    Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(isCollar);

                    Component message;
                    if (hasBinding) {
                        message = Component.translatable("item.playercollars.collar_locker.unlocked");
                        map.remove(Enchantments.BINDING_CURSE);
                    } else {
                        message = Component.translatable("item.playercollars.collar_locker.locked");
                        map.put(Enchantments.BINDING_CURSE, 1);
                    }

                    EnchantmentHelper.setEnchantments(map, isCollar);

                    player.displayClientMessage(message, true);
                    ((Player) otherEntity).displayClientMessage(message, true);
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            hasBinding ? SoundEvents.CHAIN_BREAK : SoundEvents.CHAIN_STEP, SoundSource.PLAYERS, 0.4f,
                            0.6f);
                }));
        if (!found.get()) {
            player.displayClientMessage(Component.translatable("item.playercollars.collar_locker.no_set_non_owner"),
                    true);
            return InteractionResult.PASS;
        }

        return InteractionResult.SUCCESS;
    }

}
