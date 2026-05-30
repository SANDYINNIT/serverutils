package com.serverutils.core;

// Registers blocks, items, menus, and tabs with Forge.

import com.serverutils.shop.CashRegisterBlock;
import com.serverutils.shop.CashRegisterBlockEntity;
import com.serverutils.shop.CashRegisterMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRegistries {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ServerUtils.MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ServerUtils.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ServerUtils.MODID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ServerUtils.MODID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ServerUtils.MODID);

    public static final RegistryObject<Block> CASH_REGISTER_BLOCK = BLOCKS.register("cash_register",
            () -> new CashRegisterBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.5F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> CASH_REGISTER_ITEM = ITEMS.register("cash_register",
            () -> new BlockItem(CASH_REGISTER_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<CashRegisterBlockEntity>> CASH_REGISTER_BLOCK_ENTITY = BLOCK_ENTITIES.register("cash_register",
            () -> BlockEntityType.Builder.of(CashRegisterBlockEntity::new, CASH_REGISTER_BLOCK.get()).build(null));
    public static final RegistryObject<MenuType<CashRegisterMenu>> CASH_REGISTER_MENU = MENUS.register("cash_register",
            () -> IForgeMenuType.create(CashRegisterMenu::new));
    public static final RegistryObject<CreativeModeTab> SERVER_UTILS_TAB = TABS.register("server_utils",
            () -> CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                    .title(Component.translatable("itemGroup.serverutils"))
                    .icon(() -> CASH_REGISTER_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> output.accept(CASH_REGISTER_ITEM.get()))
                    .build());

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        TABS.register(modBus);
    }

    private ModRegistries() {
    }
}
