package dev.greenhouseteam.greenhouseconfig.test.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.greenhouseteam.greenhouseconfig.api.command.GreenhouseConfigReloadCommandMethods;
import dev.greenhouseteam.greenhouseconfig.test.GreenhouseConfigTest;
import dev.greenhouseteam.greenhouseconfig.test.command.TestCommand;

public class GreenhouseConfigTestClient {
    public static void registerClientCommands(CommandDispatcher<?> dispatcher) {
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

        LiteralCommandNode<Object> colorNode = LiteralArgumentBuilder
                .literal("color")
                .build();

        LiteralCommandNode<Object> colorSplitClientNode = LiteralArgumentBuilder
                .literal("split")
                .executes(context -> TestCommand.printClientText(context, GreenhouseConfigTest.SPLIT))
                .build();

        LiteralCommandNode<Object> colorClientNode = LiteralArgumentBuilder
                .literal("client")
                .executes(context -> TestCommand.printClientText(context, GreenhouseConfigTest.CLIENT))
                .build();

        reloadNode.addChild(reloadSplitClientNode);
        reloadNode.addChild(reloadClientNode);

        colorNode.addChild(colorSplitClientNode);
        colorNode.addChild(colorClientNode);

        ghTestNode.addChild(reloadNode);
        ghTestNode.addChild(colorNode);

        ((CommandDispatcher)dispatcher).getRoot().addChild(ghTestNode);
    }
}