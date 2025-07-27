package xyz.dogboy.swp.items;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xyz.dogboy.swp.Registry;
import xyz.dogboy.swp.Utils;
import xyz.dogboy.swp.blocks.BlockPipe;
import xyz.dogboy.swp.config.SWPConfig;

public class ItemBlockPipe extends ItemBlockWoodenVariation {

    public ItemBlockPipe() {
        super(Registry.PIPE);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        super.getSubItems(tab, items);

        if (this.isInCreativeTab(tab) && SWPConfig.addRecipes) {
            for (ItemStack stone : BlockPipe.stoneVariants) {
                items.add(this.getWithBaseBlock(stone));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add("");

        if (Utils.isBurnable(this.getBaseBlock(stack))) {
            tooltip.add(I18n.format("simplewoodenpipes.tooltip.pipe.low_temp_only"));
        }

        ItemStack extractUpgrade = BlockPipe.getExtractionUpgrade();
        tooltip.add(I18n.format("simplewoodenpipes.tooltip.pipe.add_upgrade", extractUpgrade.getDisplayName()));
    }

}
