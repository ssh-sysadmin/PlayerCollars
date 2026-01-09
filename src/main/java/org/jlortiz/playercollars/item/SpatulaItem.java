package org.jlortiz.playercollars.item;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jlortiz.playercollars.PlayerCollarsMod;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class SpatulaItem extends Item{

    public SpatulaItem() {
        super(new Item.Properties().durability(8));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        //p_41433_.startUsingItem(p_41434_);
        if(!player.isCrouching())
            return super.use(level, player, hand);

        ItemStack itemstack = player.getItemInHand(hand);

        InteractionResult removeResult = RemoveCollarBinding(player);
        if (removeResult != InteractionResult.SUCCESS) {
            return new InteractionResultHolder<ItemStack>(removeResult, itemstack);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 0.4f, 1.0f);

        player.displayClientMessage(Component.translatable("item.playercollars.collar_locker.unlocked"), true);
        itemstack.hurtAndBreak(1, player, (p_150845_) -> {
            p_150845_.broadcastBreakEvent(hand);
        });

        return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, itemstack);
    }
    
    @Override
    public InteractionResult interactLivingEntity(ItemStack is, Player player, LivingEntity otherEntity,
            InteractionHand hand) {
                if(!(otherEntity instanceof Player))
            return InteractionResult.PASS;

        InteractionResult removeResult = RemoveCollarBinding((Player) otherEntity);

        if (removeResult != InteractionResult.SUCCESS) {
            return removeResult;
        }

        Component message = Component.translatable("item.playercollars.collar_locker.unlocked");
        player.displayClientMessage(message, true);
        ((Player) otherEntity).displayClientMessage(message, canRepair);
        
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 0.4f, 1.0f);

        is.hurtAndBreak(1, player, (p_150845_) -> {
            p_150845_.broadcastBreakEvent(hand);
        });

        return removeResult;
    }
    
    static InteractionResult RemoveCollarBinding(Player player)
    {
        AtomicBoolean found = new AtomicBoolean(false);
        CuriosApi.getCuriosInventory((Player) (Object) player).ifPresent((handler) -> handler.getStacksHandler("necklace").ifPresent((slot) -> {
            
            ItemStack is = null;
            IDynamicStackHandler stacks = slot.getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                ItemStack tempis = stacks.getStackInSlot(i);
                if (tempis.getItem() instanceof CollarItem) {
                    is = tempis;
                    break;
                }
            }

            if (is == null) {
                return;
            }

            boolean hasBinding = PlayerCollarsMod.COLLAR_LOCKER_ITEM.get().getEnchantmentLevel(is,
                    Enchantments.BINDING_CURSE) > 0;

            if (!hasBinding) {
                return;
            }

            found.set(true);
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(is);
            map.remove(Enchantments.BINDING_CURSE);

            EnchantmentHelper.setEnchantments(map, is);
            
        }));
        if (!found.get()) return InteractionResult.PASS;

        return InteractionResult.SUCCESS;
    }
    
}
