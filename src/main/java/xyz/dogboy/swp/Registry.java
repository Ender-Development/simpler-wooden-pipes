package xyz.dogboy.swp;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;

import xyz.dogboy.simplewoodenpipes.Tags;
import xyz.dogboy.swp.blocks.BlockPipe;
import xyz.dogboy.swp.blocks.BlockPump;
import xyz.dogboy.swp.items.ItemBlockPipe;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class Registry {
    public static final CreativeTabs CREATIVETAB = new CreativeTabs(Tags.MOD_ID) {
        @Nonnull
        @Override
        public ItemStack createIcon() {
            return new ItemStack(PUMP);
        }
    };

    public static final Block PIPE = new BlockPipe();
    public static final Item PIPE_ITEM = new ItemBlockPipe().setRegistryName(PIPE.getRegistryName());

    public static final Block PUMP = new BlockPump();
    public static final Item PUMP_ITEM = new ItemBlock(Registry.PUMP).setRegistryName(PUMP.getRegistryName());
}
