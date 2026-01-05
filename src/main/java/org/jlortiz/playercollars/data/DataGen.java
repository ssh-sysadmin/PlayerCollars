package org.jlortiz.playercollars.data;

import org.jlortiz.playercollars.PlayerCollarsMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PlayerCollarsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGen {
    
    @SubscribeEvent
    public static void gatherData(final GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        ExistingFileHelper efh = event.getExistingFileHelper();

        gen.addProvider(event.includeClient(), new ItemModelGenerator(output, PlayerCollarsMod.MOD_ID, efh));
        gen.addProvider(event.includeServer(), new RecipeGenerator(output));
    }
}