package org.jlortiz.playercollars.client.screen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import org.jlortiz.playercollars.OwnershipData;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.CollarItem;
import org.jlortiz.playercollars.network.PacketUpdateCollar;

import java.util.UUID;

public class CollarDyeScreen extends Screen {
    private final ItemStack is;
    private final CollarItem item;
    private final int initColor, initPaw;
    private final Pair<UUID, String> bondedData;
    private final boolean isBonded;
    private boolean isOwner;
    private final boolean wasOwner;

    public CollarDyeScreen(ItemStack is, UUID plr) {
        super(is.getDisplayName());
        this.is = is;
        this.item = PlayerCollarsMod.COLLAR_ITEM.get();
        initColor = item.getColor(is);
        initPaw = item.getPawColor(is);
        bondedData = OwnershipData.getBonded(is);
        if(bondedData != null)
            isBonded = bondedData.getFirst().equals(plr);
        else
            isBonded = false;
        isOwner = OwnershipData.isOwner(is, plr, null);
        wasOwner = isOwner;
    }

    @Override
    protected void init() {
        int x = this.width / 2;
        int y = this.height / 2 - 30;

        EditBox dyeField = new EditBox(this.font, x- 30, y, 100, 20, Component.empty());
        dyeField.setMaxLength(6);
        dyeField.setResponder((s) -> updateTextField(0, s));
        dyeField.setFilter((s) -> {
            try {
                Integer.parseInt(s, 16);
            } catch (NumberFormatException e) {
                return s.isEmpty();
            }
            return true;
        });
        dyeField.setValue(Integer.toHexString(initColor));

        EditBox pawField = new EditBox(this.font, x - 30, y + 25, 100, 20, Component.empty());
        pawField.setMaxLength(6);
        pawField.setResponder((s) -> updateTextField(1, s));
        pawField.setFilter((s) -> {
            try {
                Integer.parseInt(s, 16);
            } catch (NumberFormatException e) {
                return s.isEmpty();
            }
            return true;
        });
        pawField.setValue(Integer.toHexString(initPaw));

        this.addRenderableWidget(dyeField);
        this.addRenderableWidget(pawField);
        this.addRenderableWidget(Button.builder(Component.literal("Done"), (btn) -> {
            if (wasOwner != isOwner || initColor != item.getColor(is) || initColor != item.getPawColor(is)) {
                PacketUpdateCollar.OwnerState os = PacketUpdateCollar.OwnerState.NOP;
                if(wasOwner && !isOwner)
                    os = PacketUpdateCollar.OwnerState.DEL;
                else if (!wasOwner && isOwner)
                    os = PacketUpdateCollar.OwnerState.ADD;
                PlayerCollarsMod.NETWORK.sendToServer(new PacketUpdateCollar(is, os));
            }
            this.minecraft.setScreen(null);
        }).bounds(x + 5, y + 50, 75, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), (btn) -> {
            item.setColor(is, initColor);
            item.setPawColor(is, initPaw);
            this.minecraft.setScreen(null);
        }).bounds(x - 80, y + 50, 75, 20).build());

        Button ownerButton = Button.builder(Component.empty(), this::updateOwner).bounds(x - 80, y + 72, 160, 20)
                .build();
        if (!isOwner) {
            ownerButton.setMessage(Component.translatable("item.playercollars.collar.become_owner"));
        } else if (isOwner) {
            ownerButton.setMessage(Component.translatable("item.playercollars.collar.remove_owner"));
        }
        if (isBonded)
            ownerButton.active = false;

        this.addRenderableWidget(ownerButton);
    }

    private void updateOwner(Button btn) {
        if (!isOwner) {
            isOwner = true;
            btn.setMessage(Component.translatable("item.playercollars.collar.remove_owner"));
        } else {
            isOwner = false;
            btn.setMessage(Component.translatable("item.playercollars.collar.become_owner"));
            if(bondedData != null)
                btn.active = false;
        }
    }

    private void updateTextField(int i, String s) {
        int col;
        try {
            col = Integer.parseInt(s, 16);
        } catch (NumberFormatException e) {
            return;
        }
        if (i == 0) {
            item.setColor(is, col);
        } else {
            item.setPawColor(is, col);
        }
    }

    @Override
    public void render(GuiGraphics p_281549_, int mouseX, int mouseY, float delta) {
        renderBackground(p_281549_);
        super.render(p_281549_, mouseX, mouseY, delta);
        p_281549_.drawString(font, Component.translatable("item.playercollars.collar"), this.width / 2 - 75, this.height / 2 - 25, -1);
        p_281549_.drawString(font, Component.translatable("item.playercollars.collar.paw"), this.width / 2 - 75, this.height / 2 + 1, -1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
