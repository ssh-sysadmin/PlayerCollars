package org.jlortiz.playercollars;

import com.mojang.datafixers.util.Pair;

import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jlortiz.playercollars.item.ClickerItem;
import org.jlortiz.playercollars.item.CollarItem;
import org.jlortiz.playercollars.item.CollarLockerItem;
import org.jlortiz.playercollars.item.DeedItem;
import org.jlortiz.playercollars.item.OwnershipCraftingRecipe;
import org.jlortiz.playercollars.item.SpatulaItem;
import org.jlortiz.playercollars.item.StampedDeedItem;
import org.jlortiz.playercollars.network.PacketLookAtLerped;
import org.jlortiz.playercollars.network.PacketStampDeed;
import org.jlortiz.playercollars.network.PacketUpdateCollar;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioEquipEvent;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

@Mod(PlayerCollarsMod.MOD_ID)
public class PlayerCollarsMod {
	public static final String MOD_ID = "playercollars";
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
	public static final RegistryObject<CollarItem> COLLAR_ITEM = ITEMS.register("collar", CollarItem::new);
	public static final RegistryObject<ClickerItem> CLICKER_ITEM = ITEMS.register("clicker", ClickerItem::new);
	public static final RegistryObject<DeedItem> DEED_OF_OWNERSHIP = ITEMS.register("deed_of_ownership", DeedItem::new);
	public static final RegistryObject<StampedDeedItem> DEED_OF_OWNERSHIP_STAMPED = ITEMS.register("stamped_deed_of_ownership", StampedDeedItem::new);
	public static final RegistryObject<CollarLockerItem> COLLAR_LOCKER_ITEM = ITEMS.register("collar_locker", CollarLockerItem::new);
	public static final RegistryObject<SpatulaItem> SPATULA_ITEM = ITEMS.register("golden_spatula", SpatulaItem::new);
	
	public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(new ResourceLocation(MOD_ID, "collar_channel"), () -> "", String::isEmpty, String::isEmpty);
	
	private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);
	public static final RegistryObject<SoundEvent> CLICKER_ON = SOUNDS.register("clicker_on", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "clicker_on")));
	public static final RegistryObject<SoundEvent> CLICKER_OFF = SOUNDS.register("clicker_off", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "clicker_off")));
	
	private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);
	private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MOD_ID);
	public static RegistryObject<RecipeSerializer<OwnershipCraftingRecipe>> OWNERSHIP_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("owner_transfer", OwnershipCraftingRecipe.Serializer::new);

	public static final GameRules.Key<GameRules.BooleanValue> RULE_ALLOW_ATTACK_OWNER = GameRules.register("playerAllowAttackOwner", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
	public static final GameRules.Key<GameRules.IntegerValue> RULE_OWNER_ATTACK_DAMAGE_RETURNED_PERCENT = GameRules.register("playerOwnerAttackDamageReturnedPercent", GameRules.Category.PLAYER, GameRules.IntegerValue.create(75));

	public PlayerCollarsMod() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(eventBus);
		SOUNDS.register(eventBus);
		RECIPE_SERIALIZERS.register(eventBus);
		RECIPE_TYPES.register(eventBus);
		eventBus.register(this);
		MinecraftForge.EVENT_BUS.register(eventListeners.class);
		NETWORK.registerMessage(1, PacketUpdateCollar.class, PacketUpdateCollar::encode, PacketUpdateCollar::new, PacketUpdateCollar::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		NETWORK.registerMessage(2, PacketLookAtLerped.class, PacketLookAtLerped::write, PacketLookAtLerped::new, PacketLookAtLerped::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		NETWORK.registerMessage(3, PacketStampDeed.class, PacketStampDeed::encode, PacketStampDeed::new, PacketStampDeed::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
	}

	public static ItemStack filterStacksByOwner(IDynamicStackHandler stacks, UUID plr) {
		for (int i = 0; i < stacks.getSlots(); i++) {
			ItemStack is = stacks.getStackInSlot(i);
			if (is.getItem() instanceof CollarItem item) {
				Pair<UUID, String> owner = OwnershipData.getOwner(is);
				if (owner != null && owner.getFirst().equals(plr)) {
					return is;
				}
			}
		}
		return null;
	}

	@SubscribeEvent
	public void buildContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			event.accept(COLLAR_ITEM);
			event.accept(CLICKER_ITEM);
			event.accept(COLLAR_LOCKER_ITEM);
			event.accept(SPATULA_ITEM);
			event.accept(DEED_OF_OWNERSHIP);
			event.accept(DEED_OF_OWNERSHIP_STAMPED);
		}
	}

	private class eventListeners {

		@SubscribeEvent
		public static void curioEquipEvent(CurioEquipEvent event) {
			ItemStack is = event.getStack();
			if (!(is.getItem() instanceof CollarItem))
				//Result = Default
				return;

			Pair<UUID, String> ownerData = OwnershipData.getOwner(is);
			if (ownerData == null)
				//Result = Default
				return;

			Pair<UUID, String> bondedData = OwnershipData.getBonded(is);
			if (bondedData == null)
				//Result = Default
				return;

			LivingEntity entity = event.getEntity();
			if (!(entity instanceof Player))
				//Result = Default
				return;

			Player player = (Player) entity;
			if (!player.getUUID().equals(bondedData.getFirst())) {
				event.setResult(Result.DENY);
				return;
			}

			event.setResult(Result.ALLOW);
		}

		@SubscribeEvent
		public static void attackEntityEvent(AttackEntityEvent event)
		{
			Entity target = event.getTarget();
			if (!(target instanceof Player)) {
				return;
			}

			Player targetPlayer = (Player) target;

			AtomicBoolean denyAttack = new AtomicBoolean(false);

			CuriosApi.getCuriosInventory(event.getEntity())
					.ifPresent((handler) -> handler.getStacksHandler("necklace").ifPresent((slot) -> {

						boolean targetIsOwner = false;
						IDynamicStackHandler stacks = slot.getStacks();
						for (int i = 0; i < stacks.getSlots(); i++) {
							ItemStack tempis = stacks.getStackInSlot(i);
							if (tempis.getItem() instanceof CollarItem) {
								Pair<UUID, String> ownerData = OwnershipData.getOwner(tempis);
								if (ownerData != null && ownerData.getFirst().equals(targetPlayer.getUUID())) {
									targetIsOwner = true;
									break;
								}
							}
						}

						if(!targetIsOwner)
							return;

						Player attacker = event.getEntity();

						if (!attacker.level().getGameRules().getBoolean(RULE_ALLOW_ATTACK_OWNER)) {
							denyAttack.set(true);
							return;
						}

						double damage = attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
						double damagePercent = attacker.level().getGameRules().getInt(RULE_OWNER_ATTACK_DAMAGE_RETURNED_PERCENT) / 100.0D;
						damage = damage * damagePercent;
						if (damage > 0)
							damage = Math.ceil(damage);
						else
							damage = Math.floor(damage);

						if (damage > 0) {
							attacker.displayClientMessage(Component.translatable("message.playercollars.no_attack_owner").withStyle(ChatFormatting.RED),
									true);
							attacker.hurt(attacker.damageSources().playerAttack(attacker), (float) damage);
						} else if (damage < 0) {
							attacker.heal((float) -damage);
						}
					}));

			if(denyAttack.get())
			{
				event.setCanceled(true);
			}
			
		}
	}
}
