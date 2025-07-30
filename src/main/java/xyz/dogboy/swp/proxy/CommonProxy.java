package xyz.dogboy.swp.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import xyz.dogboy.swp.Utils;
import xyz.dogboy.swp.config.CfgParser;
import xyz.dogboy.swp.config.SWPConfig;
import xyz.dogboy.swp.event.CommonEventHandler;

public class CommonProxy implements IProxy {
    public static CfgParser.ConfigItem DEFAULT_MATERIAL;

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        // Common pre-initialization logic
        MinecraftForge.EVENT_BUS.register(new CommonEventHandler());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        // Common initialization logic
        DEFAULT_MATERIAL = new CfgParser.ConfigItem(SWPConfig.defaultBaseBlock);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        // Common post-initialization logic
        Utils.whitelist.init();
        Utils.blacklist.init();
    }
}
