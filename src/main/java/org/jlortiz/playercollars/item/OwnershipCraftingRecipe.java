package org.jlortiz.playercollars.item;

import java.util.ArrayList;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.OwnershipData;
import org.jlortiz.playercollars.PlayerCollarsMod;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record OwnershipCraftingRecipe(ResourceLocation id, Ingredient base, CraftingBookCategory category) implements CraftingRecipe {

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        int deedCount = 0;
        int baseCount = 0;

        UUID bondedUUID = null;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack is = container.getItem(i);
            if (base.test(is)) {
                if (baseCount == 1)
                    return false;

                baseCount++;

                Pair<UUID, String> bondedData = OwnershipData.getBonded(is);
                if (bondedData != null) {
                    if (bondedUUID == null) {
                        bondedUUID = bondedData.getFirst();
                    }
                    else {
                        if (!(bondedData.getFirst().equals(bondedUUID)))
                            return false;
                    }
                }
            } else if (is.getItem() instanceof StampedDeedItem) {
                Pair<UUID, String> bondedData = OwnershipData.getBonded(is);
                if (bondedUUID == null) {
                    bondedUUID = bondedData.getFirst();
                } else {
                    if (!(bondedData.getFirst().equals(bondedUUID)))
                        return false;
                }

                deedCount++;
            } else {
                if(!is.isEmpty())
                    return false;
            }

        }

        if (deedCount == 0 || baseCount != 1)
            return false;

        return true;
    }

    @SuppressWarnings("null")
    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess regAccess) {
        ItemStack output = null;
        ArrayList<Pair<UUID, String>> newOwners = new ArrayList<>();
        Pair<UUID, String> outputBondedData = null;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack is = container.getItem(i);
            if (base.test(is)) {
                Pair<UUID, String> bondedData = OwnershipData.getBonded(is);
                if (bondedData != null) {
                    if (outputBondedData == null) {
                        outputBondedData = bondedData;
                    } else {
                        if (!(bondedData.getFirst().equals(outputBondedData.getFirst()))) {
                            output = is.copy();
                            newOwners.clear();
                            break;
                        }
                    }
                }

                if(output == null)
                    output = is.copy();

            } else if (is.getItem() instanceof StampedDeedItem) {
                Pair<UUID, String> bondedData = OwnershipData.getBonded(is);
                if (bondedData != null) {
                    if (outputBondedData == null) {
                        outputBondedData = bondedData;
                    } else {
                        if (!(bondedData.getFirst().equals(outputBondedData.getFirst()))) {
                            newOwners.clear();
                            break;
                        }
                    }
                }

                if (OwnershipData.getOwnersCount(is) > 0)
                    // Realistically will only have one
                    newOwners.addAll(OwnershipData.getOwnersArrayList(is));
                
            }
        }

        if (output == null)
            return base.getItems()[0];

        if (newOwners.size() == 0 || outputBondedData == null)
            return output.copy();

        for (Pair<UUID, String> owner : newOwners)
        {
            OwnershipData.addOwner(output, owner.getFirst(), owner.getSecond());
        }
        OwnershipData.setBonded(output, outputBondedData.getFirst(), outputBondedData.getSecond());
        return output;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        if (width * height > 1)
            return true;

        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_267052_) {
        ItemStack is = base.getItems()[0];
        OwnershipData.addOwner(is, new UUID(0L, 0L), "Deed Owner");
        return is;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PlayerCollarsMod.OWNERSHIP_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    public static class Serializer implements RecipeSerializer<OwnershipCraftingRecipe>
    {
        @Override
        public OwnershipCraftingRecipe fromJson(ResourceLocation resLoc, JsonObject json) {
            Ingredient base = Ingredient.fromJson(json.get("base"));
            CraftingBookCategory craftingbookcategory = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(json, "category", (String)null), CraftingBookCategory.MISC);
            return new OwnershipCraftingRecipe(resLoc, base, craftingbookcategory);
        }

        @Override
        public @Nullable OwnershipCraftingRecipe fromNetwork(ResourceLocation resLoc, FriendlyByteBuf buffer) {
            Ingredient base = Ingredient.fromNetwork(buffer);
            CraftingBookCategory craftingbookcategory = buffer.readEnum(CraftingBookCategory.class);
            return new OwnershipCraftingRecipe(resLoc, base, craftingbookcategory);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, OwnershipCraftingRecipe recipe) {
            recipe.base.toNetwork(buffer);
            buffer.writeEnum(recipe.category);
         }
    
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

}

