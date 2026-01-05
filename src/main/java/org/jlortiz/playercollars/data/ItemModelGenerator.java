package org.jlortiz.playercollars.data;

import org.jlortiz.playercollars.PlayerCollarsMod;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemModelGenerator extends ItemModelProvider {

    public ItemModelGenerator(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
        //TODO Auto-generated constructor stub
    }

    @Override
    protected void registerModels() {
        this.basicItem(PlayerCollarsMod.COLLAR_LOCKER_ITEM.get());

        ResourceLocation rlSpatula = ForgeRegistries.ITEMS.getKey(PlayerCollarsMod.SPATULA_ITEM.get());
        getBuilder(rlSpatula.toString())
        .parent(new ModelFile.UncheckedModelFile("item/handheld"))
        .texture("layer0", new ResourceLocation(rlSpatula.getNamespace(), "item/" + rlSpatula.getPath()));
    }
    
}
