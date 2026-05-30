package com.serverutils.shop;

// Stores cash register owner, stock, and slot prices.

import com.serverutils.core.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

public final class CashRegisterBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_COUNT = 9;
    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> inventory);
    private final int[] prices = new int[SLOT_COUNT];
    private UUID owner;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index >= 0 && index < SLOT_COUNT ? prices[index] : 0;
        }

        @Override
        public void set(int index, int value) {
            if (index >= 0 && index < SLOT_COUNT) {
                prices[index] = Math.max(0, value);
                setChanged();
            }
        }

        @Override
        public int getCount() {
            return SLOT_COUNT;
        }
    };

    public CashRegisterBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.CASH_REGISTER_BLOCK_ENTITY.get(), pos, state);
        Arrays.fill(prices, 10);
    }

    public UUID owner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public ItemStack getStack(int slot) {
        return inventory.getStackInSlot(slot);
    }

    public ItemStack extract(int slot, int amount) {
        return inventory.extractItem(slot, amount, false);
    }

    public int getPrice(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? prices[slot] : 0;
    }

    public void adjustPrice(int slot, int delta) {
        if (slot >= 0 && slot < SLOT_COUNT) {
            prices[slot] = Math.max(0, prices[slot] + delta);
            setChanged();
        }
    }

    public ContainerData data() {
        return data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.serverutils.cash_register");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CashRegisterMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
        tag.put("inventory", inventory.serializeNBT(provider));
        tag.putIntArray("prices", prices);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.hasUUID("owner")) {
            owner = tag.getUUID("owner");
        }
        if (tag.contains("inventory", 10)) {
            inventory.deserializeNBT(provider, tag.getCompound("inventory"));
        }
        int[] loadedPrices = tag.getIntArray("prices");
        for (int i = 0; i < Math.min(loadedPrices.length, prices.length); i++) {
            prices[i] = loadedPrices[i];
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction direction) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, direction);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }
}
