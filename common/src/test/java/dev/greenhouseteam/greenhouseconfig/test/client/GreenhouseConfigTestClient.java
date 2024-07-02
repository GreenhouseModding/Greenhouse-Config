package dev.greenhouseteam.greenhouseconfig.test.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.greenhouseteam.greenhouseconfig.api.command.GreenhouseConfigReloadCommandMethods;
import dev.greenhouseteam.greenhouseconfig.test.GreenhouseConfigTest;

public class GreenhouseConfigTestClient {
    public static void registerClientReloadCommands(CommandDispatcher<Object> dispatcher) {
        LiteralCommandNode<Object> ghTestNode = LiteralArgumentBuilder
                .literal("greenhousetestclient")
                .build();

        LiteralCommandNode<Object> reloadNode = LiteralArgumentBuilder
                .literal("reload")
                .build();

        LiteralCommandNode<Object> reloadSplitClientNode = LiteralArgumentBuilder
                .literal("split")
                .executes(context -> GreenhouseConfigReloadCommandMethods.reloadGreenhouseConfigClient(context, GreenhouseConfigTest.SPLIT))
                .build();

        LiteralCommandNode<Object> reloadClientNode = LiteralArgumentBuilder
                .literal("client")
                .executes(context -> GreenhouseConfigReloadCommandMethods.reloadGreenhouseConfigClient(context, GreenhouseConfigTest.CLIENT))
                .build();

        reloadNode.addChild(reloadSplitClientNode);
        reloadNode.addChild(reloadClientNode);

        ghTestNode.addChild(reloadNode);

        dispatcher.getRoot().addChild(ghTestNode);
    }
}
