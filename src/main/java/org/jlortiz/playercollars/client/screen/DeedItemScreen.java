package org.jlortiz.playercollars.client.screen;

import org.jlortiz.playercollars.OwnershipData;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.network.PacketStampDeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DeedItemScreen extends Screen {
    private final Component ownerName;
    private final Player player;

    public DeedItemScreen(ItemStack is, Player plr) {
        super(is.getDisplayName());
        if (OwnershipData.getOwnersCount(is) == 0)
            this.ownerName = Component.literal("");
        else
            this.ownerName = Component.literal(OwnershipData.getOwnersArrayList(is).get(0).getSecond());
        this.player = plr;
    }
    
    @SuppressWarnings("null")
    @Override
    protected void init() {
        this.addRenderableWidget(
                Button.builder(Component.translatable("item.playercollars.deed_of_ownership.stamp"), (this::stampDeed))
        .bounds(this.width / 2 - 80, this.height / 2 + 72, 160, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), (btn) -> {
            this.minecraft.setScreen(null);
        }).bounds(this.width / 2 - 80, this.height / 2 + 95, 160, 20).build());
    }

    @Override
    public void render(GuiGraphics p_281549_, int mouseX, int mouseY, float delta) {
        renderBackground(p_281549_);
        super.render(p_281549_, mouseX, mouseY, delta);

        p_281549_.drawCenteredString(font, Component.translatable("item.playercollars.deed_of_ownership"), this.width / 2, this.height / 2 - 88, -1);
        p_281549_.drawCenteredString(font, Component.translatable("item.playercollars.deed_of_ownership.line1", player.getName(), ownerName), this.width / 2, this.height / 2 - 55, -1);
        p_281549_.drawCenteredString(font, Component.translatable("item.playercollars.deed_of_ownership.line2"), this.width / 2, this.height / 2 - 38, -1);
        p_281549_.drawCenteredString(font, Component.translatable("item.playercollars.deed_of_ownership.line3"), this.width / 2, this.height / 2 - 26, -1);
        p_281549_.drawCenteredString(font, Component.translatable("item.playercollars.deed_of_ownership.line4"), this.width / 2, this.height / 2 - 14, -1);
        p_281549_.drawCenteredString(font, Component.translatable("item.playercollars.deed_of_ownership.line5"), this.width / 2, this.height / 2 - 2, -1);
        p_281549_.drawCenteredString(font, Component.translatable("item.playercollars.deed_of_ownership.line6"), this.width / 2, this.height / 2 + 10, -1);
        p_281549_.drawCenteredString(font, Component.translatable("item.playercollars.deed_of_ownership.line7"), this.width / 2, this.height / 2 + 23, -1);
        p_281549_.drawCenteredString(font, Component.translatable("item.playercollars.deed_of_ownership.line8"), this.width / 2, this.height / 2 + 40, -1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void stampDeed(Button btn)
    {
        PlayerCollarsMod.NETWORK.sendToServer(new PacketStampDeed());
        Minecraft.getInstance().setScreen(null);
    }

}
