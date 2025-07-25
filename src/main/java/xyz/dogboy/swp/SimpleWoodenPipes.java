package xyz.dogboy.swp;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.dogboy.simplewoodenpipes.Tags;
import xyz.dogboy.swp.proxy.IProxy;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version =Tags.VERSION)
public class SimpleWoodenPipes {

    @Mod.Instance(Tags.MOD_ID)
    public static SimpleWoodenPipes instance;

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @SidedProxy(modId = Tags.MOD_ID, clientSide = "xyz.dogboy.swp.proxy.ClientProxy", serverSide = "xyz.dogboy.swp.proxy.CommonProxy")
    public static IProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
