package org.jlortiz.playercollars.data;

import java.util.function.Consumer;

import org.jlortiz.playercollars.PlayerCollarsMod;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

public class RecipeGenerator extends RecipeProvider {
    
    public RecipeGenerator(PackOutput p_248933_) {
        super(p_248933_);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> p_251297_) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, PlayerCollarsMod.COLLAR_LOCKER_ITEM.get())
        .requires(Items.CHAIN)
        .requires(Items.CHAIN)
        .requires(Items.IRON_BARS)
        .requires(Items.REDSTONE)
        .unlockedBy("has_collar", has(PlayerCollarsMod.COLLAR_ITEM.get()))
        .save(p_251297_);
        

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, PlayerCollarsMod.SPATULA_ITEM.get())
        .pattern("  g")
        .pattern(" g ")
        .pattern("s  ")
        .define('g', Items.GOLD_INGOT)
        .define('s', Items.STICK)
        .unlockedBy("has_gold", has(Items.GOLD_INGOT))
        .save(p_251297_);
    }
    
}
