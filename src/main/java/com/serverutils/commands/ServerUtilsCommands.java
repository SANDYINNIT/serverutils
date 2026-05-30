package com.serverutils.commands;

// Registers the player and admin commands for Server Utils.

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.serverutils.claims.ClaimSavedData;
import com.serverutils.claims.ClaimService;
import com.serverutils.costs.Costs;
import com.serverutils.economy.EconomyService;
import com.serverutils.teleport.StoredLocation;
import com.serverutils.teleport.TeleportSavedData;
import com.serverutils.teleport.TeleportService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public final class ServerUtilsCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerEconomy(dispatcher);
        registerClaims(dispatcher);
        registerTeleport(dispatcher);
        dispatcher.register(Commands.literal("serverutils")
                .then(Commands.literal("balance").executes(ctx -> balance(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("claims").executes(ctx -> claims(ctx.getSource().getPlayerOrException()))));
    }

    private static void registerEconomy(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("balance")
                .executes(ctx -> balance(ctx.getSource().getPlayerOrException())));
        dispatcher.register(Commands.literal("pay")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                .executes(ctx -> pay(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player"), LongArgumentType.getLong(ctx, "amount"))))));
        dispatcher.register(Commands.literal("baltop").executes(ctx -> baltop(ctx.getSource())));
        dispatcher.register(Commands.literal("eco").requires(source -> source.hasPermission(2))
                .then(Commands.literal("give").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("amount", LongArgumentType.longArg(1)).executes(ctx -> ecoGive(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), LongArgumentType.getLong(ctx, "amount"))))))
                .then(Commands.literal("take").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("amount", LongArgumentType.longArg(1)).executes(ctx -> ecoTake(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), LongArgumentType.getLong(ctx, "amount"))))))
                .then(Commands.literal("set").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("amount", LongArgumentType.longArg(0)).executes(ctx -> ecoSet(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), LongArgumentType.getLong(ctx, "amount")))))));
    }

    private static void registerClaims(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim").executes(ctx -> ClaimService.INSTANCE.claim(ctx.getSource().getPlayerOrException()) ? 1 : 0));
        dispatcher.register(Commands.literal("unclaim").executes(ctx -> ClaimService.INSTANCE.unclaim(ctx.getSource().getPlayerOrException()) ? 1 : 0));
        dispatcher.register(Commands.literal("claims").executes(ctx -> claims(ctx.getSource().getPlayerOrException())));
        dispatcher.register(Commands.literal("trust").then(Commands.argument("player", EntityArgument.player()).executes(ctx -> ClaimService.INSTANCE.trust(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")) ? 1 : 0)));
        dispatcher.register(Commands.literal("untrust").then(Commands.argument("player", EntityArgument.player()).executes(ctx -> ClaimService.INSTANCE.untrust(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")) ? 1 : 0)));
    }

    private static void registerTeleport(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sethome")
                .executes(ctx -> setHome(ctx.getSource().getPlayerOrException(), "home"))
                .then(Commands.argument("name", StringArgumentType.word()).executes(ctx -> setHome(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "name")))));
        dispatcher.register(Commands.literal("home")
                .executes(ctx -> home(ctx.getSource().getPlayerOrException(), "home"))
                .then(Commands.argument("name", StringArgumentType.word()).executes(ctx -> home(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "name")))));
        dispatcher.register(Commands.literal("delhome").then(Commands.argument("name", StringArgumentType.word()).executes(ctx -> delHome(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "name")))));
        dispatcher.register(Commands.literal("homes").executes(ctx -> homes(ctx.getSource().getPlayerOrException())));
        dispatcher.register(Commands.literal("tpa").then(Commands.argument("player", EntityArgument.player()).executes(ctx -> tpa(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))));
        dispatcher.register(Commands.literal("tpaccept").executes(ctx -> {
            TeleportService.INSTANCE.accept(ctx.getSource().getPlayerOrException());
            return 1;
        }));
        dispatcher.register(Commands.literal("tpdeny").executes(ctx -> {
            TeleportService.INSTANCE.deny(ctx.getSource().getPlayerOrException());
            return 1;
        }));
        dispatcher.register(Commands.literal("setspawn").requires(source -> source.hasPermission(2)).executes(ctx -> setSpawn(ctx.getSource().getPlayerOrException())));
        dispatcher.register(Commands.literal("spawn").executes(ctx -> spawn(ctx.getSource().getPlayerOrException())));
        dispatcher.register(Commands.literal("back").executes(ctx -> back(ctx.getSource().getPlayerOrException())));
    }

    private static int balance(ServerPlayer player) {
        long balance = EconomyService.INSTANCE.getBalance(player.server, player.getUUID());
        player.sendSystemMessage(Component.translatable("commands.serverutils.balance", balance));
        return 1;
    }

    private static int pay(ServerPlayer sender, ServerPlayer target, long amount) {
        var result = EconomyService.INSTANCE.transfer(sender.server, sender.getUUID(), target.getUUID(), amount, Costs.INSTANCE.payTaxPercent());
        sender.sendSystemMessage(Component.translatable(result.messageKey(), target.getGameProfile().getName(), amount, result.tax()));
        return result.success() ? 1 : 0;
    }

    private static int baltop(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("commands.serverutils.baltop.header"), false);
        int rank = 1;
        for (Map.Entry<java.util.UUID, Long> entry : EconomyService.INSTANCE.topBalances(source.getServer(), 10).entrySet()) {
            String name = source.getServer().getProfileCache().get(entry.getKey()).map(profile -> profile.getName()).orElse(entry.getKey().toString());
            int currentRank = rank++;
            source.sendSuccess(() -> Component.translatable("commands.serverutils.baltop.entry", currentRank, name, entry.getValue()), false);
        }
        return 1;
    }

    private static int ecoGive(CommandSourceStack source, ServerPlayer target, long amount) {
        EconomyService.INSTANCE.deposit(source.getServer(), target.getUUID(), amount);
        source.sendSuccess(() -> Component.translatable("commands.serverutils.eco.give", amount, target.getGameProfile().getName()), true);
        return 1;
    }

    private static int ecoTake(CommandSourceStack source, ServerPlayer target, long amount) {
        EconomyService.INSTANCE.withdraw(source.getServer(), target.getUUID(), amount);
        source.sendSuccess(() -> Component.translatable("commands.serverutils.eco.take", amount, target.getGameProfile().getName()), true);
        return 1;
    }

    private static int ecoSet(CommandSourceStack source, ServerPlayer target, long amount) {
        EconomyService.INSTANCE.setBalance(source.getServer(), target.getUUID(), amount);
        source.sendSuccess(() -> Component.translatable("commands.serverutils.eco.set", target.getGameProfile().getName(), amount), true);
        return 1;
    }

    private static int claims(ServerPlayer player) {
        long count = ClaimSavedData.get(player.server).claims().values().stream().filter(claim -> claim.owner().equals(player.getUUID())).count();
        player.sendSystemMessage(Component.translatable("commands.serverutils.claims.count", count));
        return 1;
    }

    private static int setHome(ServerPlayer player, String name) {
        TeleportSavedData data = TeleportSavedData.get(player.server);
        if (!data.homes(player.getUUID()).containsKey(name.toLowerCase()) && data.homes(player.getUUID()).size() >= Costs.INSTANCE.maxHomes()) {
            player.sendSystemMessage(Component.translatable("commands.serverutils.home.limit"));
            return 0;
        }
        if (!TeleportService.INSTANCE.charge(player, Costs.INSTANCE.setHomeCost())) {
            return 0;
        }
        data.setHome(player.getUUID(), name, StoredLocation.of(player));
        player.sendSystemMessage(Component.translatable("commands.serverutils.home.set", name));
        return 1;
    }

    private static int home(ServerPlayer player, String name) {
        return TeleportSavedData.get(player.server).getHome(player.getUUID(), name).map(location -> {
            if (!TeleportService.INSTANCE.charge(player, Costs.INSTANCE.homeCost())) {
                return 0;
            }
            TeleportService.INSTANCE.teleport(player, location);
            return 1;
        }).orElseGet(() -> {
            player.sendSystemMessage(Component.translatable("commands.serverutils.home.missing", name));
            return 0;
        });
    }

    private static int delHome(ServerPlayer player, String name) {
        boolean removed = TeleportSavedData.get(player.server).deleteHome(player.getUUID(), name);
        player.sendSystemMessage(Component.translatable(removed ? "commands.serverutils.home.deleted" : "commands.serverutils.home.missing", name));
        return removed ? 1 : 0;
    }

    private static int homes(ServerPlayer player) {
        String names = String.join(", ", TeleportSavedData.get(player.server).homes(player.getUUID()).keySet());
        player.sendSystemMessage(Component.translatable("commands.serverutils.homes", names.isBlank() ? "-" : names));
        return 1;
    }

    private static int tpa(ServerPlayer player, ServerPlayer target) {
        TeleportService.INSTANCE.request(player, target);
        return 1;
    }

    private static int setSpawn(ServerPlayer player) {
        TeleportSavedData.get(player.server).setSpawn(StoredLocation.of(player));
        player.sendSystemMessage(Component.translatable("commands.serverutils.spawn.set"));
        return 1;
    }

    private static int spawn(ServerPlayer player) {
        return TeleportSavedData.get(player.server).getSpawn().map(location -> {
            if (!TeleportService.INSTANCE.charge(player, Costs.INSTANCE.spawnCost())) {
                return 0;
            }
            TeleportService.INSTANCE.teleport(player, location);
            return 1;
        }).orElseGet(() -> {
            player.sendSystemMessage(Component.translatable("commands.serverutils.spawn.missing"));
            return 0;
        });
    }

    private static int back(ServerPlayer player) {
        return TeleportSavedData.get(player.server).getBack(player.getUUID()).map(location -> {
            if (!TeleportService.INSTANCE.charge(player, Costs.INSTANCE.backCost())) {
                return 0;
            }
            TeleportService.INSTANCE.teleport(player, location);
            return 1;
        }).orElseGet(() -> {
            player.sendSystemMessage(Component.translatable("commands.serverutils.back.missing"));
            return 0;
        });
    }

    private ServerUtilsCommands() {
    }
}
