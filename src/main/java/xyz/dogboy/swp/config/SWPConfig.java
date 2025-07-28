package xyz.dogboy.swp.config;

import net.minecraftforge.common.config.Config;

import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.dogboy.simplewoodenpipes.Tags;

@Config(modid = Tags.MOD_ID, name = Tags.MOD_ID, type = Config.Type.INSTANCE)
public class SWPConfig {

    @Config.RangeInt(min = 1)
    public static int internalVolume = 1000;

    @Config.RangeInt(min = 1)
    public static int pumpRate = 25;

    @Config.RangeInt(min = 1)
    public static int transferRate = 50;

    @Config.RequiresMcRestart
    @Config.Comment("Should recipes be added by default?")
    public static boolean addRecipes = true;

    @Config.Comment("Item used to upgrade a pipe to extract mode. Format: <mod>:<block>:<meta>")
    public static String pipeExtractionItem = "minecraft:piston";

    @Config.Comment("Should Pipes connect to variations of the same block variant (allow all wood variants to connect to each other for example)")
    public static boolean variantInterconnection = true;

    @Config.RequiresMcRestart
    @Config.Comment("Whitelist of block names that will burn even if the block is not normally flammable. Format: <mod>:<block>:<meta>")
    public static String[] burnableBlocks = new String[]{};

    @Config.RequiresMcRestart
    @Config.Comment("Blacklist of block names that will not burn even if the block is normally flammable. Format: <mod>:<block>:<meta>")
    public static String[] nonBurnableBlocks = new String[]{};

    @Config.Comment("Utilize advanced naming scheme. This allows renaming individual pipes. ONLY ENABLE IF YOU KNOW WHAT YOU ARE DOING!")
    public static boolean advancedNamingScheme = false;

    @Config.RequiresMcRestart
    @Config.Comment("Default base block for pipes. Format: <mod>:<block>:<meta>")
    public static String defaultBaseBlock = "minecraft:planks";

    @Mod.EventBusSubscriber(modid = Tags.MOD_ID)
    public static class ConfigEventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Tags.MOD_ID)) {
                ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
            }
        }
    }

}
