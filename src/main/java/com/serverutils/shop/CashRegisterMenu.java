package com.serverutils.shop;

// Syncs cash register inventory and price controls.

import com.serverutils.core.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public final class CashRegisterMenu extends AbstractContainerMenu {
    private final CashRegisterBlockEntity register;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public CashRegisterMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, resolve(playerInventory, buffer.readBlockPos()));
    }

    public CashRegisterMenu(int containerId, Inventory playerInventory, CashRegisterBlockEntity register) {
        super(ModRegistries.CASH_REGISTER_MENU.get(), containerId);
        this.register = register;
        this.data = register == null ? new SimpleContainerData(CashRegisterBlockEntity.SLOT_COUNT) : register.data();
        this.access = register == null || register.getLevel() == null
                ? ContainerLevelAccess.NULL
                : ContainerLevelAccess.create(register.getLevel(), register.getBlockPos());

        if (register != null) {
            for (int slot = 0; slot < CashRegisterBlockEntity.SLOT_COUNT; slot++) {
                addSlot(new SlotItemHandler(register.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new), slot, 8 + slot * 18, 20));
            }
        }
        addPlayerInventory(playerInventory);
        addDataSlots(data);
    }

    private static CashRegisterBlockEntity resolve(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof CashRegisterBlockEntity blockEntity) {
            return blockEntity;
        }
        return null;
    }

    public int price(int slot) {
        return data.get(slot);
    }

    public ItemStack displayStack(int slot) {
        return register == null ? ItemStack.EMPTY : register.getStack(slot);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (!(player instanceof ServerPlayer serverPlayer) || register == null) {
            return false;
        }
        if (buttonId >= 0 && buttonId < CashRegisterBlockEntity.SLOT_COUNT) {
            return CashRegisterTransactionService.INSTANCE.buy(serverPlayer, register, buttonId);
        }
        if (buttonId >= 100 && buttonId < 100 + CashRegisterBlockEntity.SLOT_COUNT && serverPlayer.getUUID().equals(register.owner())) {
            register.adjustPrice(buttonId - 100, 10);
            return true;
        }
        if (buttonId >= 200 && buttonId < 200 + CashRegisterBlockEntity.SLOT_COUNT && serverPlayer.getUUID().equals(register.owner())) {
            register.adjustPrice(buttonId - 200, -10);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < CashRegisterBlockEntity.SLOT_COUNT) {
                if (!moveItemStackTo(stack, CashRegisterBlockEntity.SLOT_COUNT, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, CashRegisterBlockEntity.SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModRegistries.CASH_REGISTER_BLOCK.get());
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 70 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 128));
        }
    }
}
