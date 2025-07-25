package xyz.dogboy.swp.config;

import net.minecraft.item.ItemStack;
import xyz.dogboy.swp.SimpleWoodenPipes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class CfgHandler<T extends CfgParser.ConfigItem> {
    private final List<T> configItems = new ArrayList<>();
    private final String[] configData;
    private final Function<String, T> parser;

    public CfgHandler(String[] configData, Function<String, T> parser) {
        this.configData = configData;
        this.parser = parser;
    }

    public CfgHandler<T> init() {
        for (String line : configData) {
            try {
                T entry = parser.apply(line);
                configItems.add(entry);
            } catch (Exception e) {
                SimpleWoodenPipes.LOGGER.error("Error parsing config data: {}", line, e);
            }
        }
        return this;
    }

    /**
     * Check if the list contains the given item stack.
     * @param stack The item stack to check.
     * @return True if the list contains the item stack, false otherwise.
     */
    public boolean contains(ItemStack stack) {
        return configItems.stream().anyMatch(item -> item.compare(stack));
    }
}
