package house.greenhouse.greenhouseconfig.test.command;

import com.mojang.brigadier.context.CommandContext;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder;
import house.greenhouse.greenhouseconfig.impl.GreenhouseConfig;
import house.greenhouse.greenhouseconfig.test.config.SplitConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class TestCommand {
    public static int printServerText(CommandContext<CommandSourceStack> context, GreenhouseConfigHolder<?> holder) {
        if (!(holder.get() instanceof SplitConfig splitConfig))
            return 0;
        context.getSource().sendSuccess(() -> Component.literal(splitConfig.color().serialize()).withColor(splitConfig.color().getValue()), false);
        return 1;
    }

    public static int printClientText(CommandContext<?> context, GreenhouseConfigHolder<?> holder) {
        if (!(holder.get() instanceof SplitConfig splitConfig))
            return 0;
        GreenhouseConfig.getPlatform().sendSuccessClient(context, Component.literal(splitConfig.clientValues().color().serialize()).withColor(splitConfig.clientValues().color().getValue()));
        return 1;
    }
}
