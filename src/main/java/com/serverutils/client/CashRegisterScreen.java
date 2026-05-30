package com.serverutils.client;

// Draws the simple client GUI for the cash register.

import com.serverutils.shop.CashRegisterBlockEntity;
import com.serverutils.shop.CashRegisterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class CashRegisterScreen extends AbstractContainerScreen<CashRegisterMenu> {
    public CashRegisterScreen(CashRegisterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        for (int slot = 0; slot < CashRegisterBlockEntity.SLOT_COUNT; slot++) {
            int x = leftPos + 7 + slot * 18;
            int index = slot;
            addRenderableWidget(Button.builder(Component.literal("$"), button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index))
                    .bounds(x, topPos + 42, 18, 16)
                    .build());
            addRenderableWidget(Button.builder(Component.literal("+"), button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 100 + index))
                    .bounds(x, topPos + 4, 18, 12)
                    .build());
            addRenderableWidget(Button.builder(Component.literal("-"), button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 200 + index))
                    .bounds(x, topPos + 56, 18, 12)
                    .build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 4210752, false);
        for (int slot = 0; slot < CashRegisterBlockEntity.SLOT_COUNT; slot++) {
            graphics.drawString(font, String.valueOf(menu.price(slot)), 9 + slot * 18, 33, 4210752, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFB8B8B8);
        graphics.fill(leftPos + 6, topPos + 18, leftPos + 170, topPos + 60, 0xFF707070);
        graphics.fill(leftPos + 6, topPos + 68, leftPos + 170, topPos + 152, 0xFFE0E0E0);
    }
}
