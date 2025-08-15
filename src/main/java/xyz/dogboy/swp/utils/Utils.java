package xyz.dogboy.swp.utils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import xyz.dogboy.swp.config.CfgHandler;
import xyz.dogboy.swp.config.CfgParser;
import xyz.dogboy.swp.config.SWPConfig;

import java.util.List;

public class Utils {
    public static final CfgHandler<CfgParser.ConfigItem> whitelist = new CfgHandler<>(
            SWPConfig.burnableBlocks,
            CfgParser.ConfigItem::new
    );

    public static final CfgHandler<CfgParser.ConfigItem> blacklist = new CfgHandler<>(
            SWPConfig.nonBurnableBlocks,
            CfgParser.ConfigItem::new
    );

    public static List<ItemStack> getAllPlanks() {
        NonNullList<ItemStack> planks = NonNullList.create();

        for (ItemStack plank : OreDictionary.getOres("plankWood")) {
            if (plank.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                plank.getItem().getSubItems(CreativeTabs.SEARCH, planks);
            } else {
                planks.add(plank);
            }
        }
        return planks;
    }

    public static boolean isBurnable(ItemStack stack) {
        if (stack.isEmpty() || blacklist.contains(stack)) {
            return false;
        }
        if (whitelist.contains(stack)) {
            return true;
        }
        Block block = Block.getBlockFromItem(stack.getItem());
        if (block == Blocks.AIR) {
            return false;
        }
        IBlockState state = block.getDefaultState();
        return state.getMaterial().getCanBurn();
    }

    public static EnumFacing readNullableFacing(int index) {
        return index > 0 ? EnumFacing.byIndex(index) : null;
    }

    public static int writeNullableFacing(EnumFacing facing) {
        return facing != null ? facing.getIndex() : -1;
    }

    @SideOnly(Side.CLIENT)
    public static String getTextureFromBlock(Block block, int meta) {
        IBlockState state = block.getStateFromMeta(meta);
        return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state).getIconName();
    }
}
