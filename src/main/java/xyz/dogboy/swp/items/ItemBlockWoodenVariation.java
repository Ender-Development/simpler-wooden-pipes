package xyz.dogboy.swp.items;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import xyz.dogboy.swp.Utils;
import xyz.dogboy.swp.config.SWPConfig;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockWoodenVariation extends ItemBlock {

    public ItemBlockWoodenVariation(Block block) {
        super(block);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab) && SWPConfig.addRecipes) {
            for (ItemStack plank : Utils.getAllPlanks()) {
                items.add(this.getWithBaseBlock(plank));
            }
        }
    }

    public ItemStack getWithBaseBlock(ItemStack baseBlock) {
        ItemStack item = new ItemStack(this);

        if (!item.hasTagCompound()) {
            item.setTagCompound(new NBTTagCompound());
        }

        NBTTagCompound baseBlockNbt = new NBTTagCompound();
        baseBlock.writeToNBT(baseBlockNbt);
        item.getTagCompound().setTag("BaseBlock", baseBlockNbt);
        return item;
    }

    public ItemStack getBaseBlock(ItemStack item) {
        if (item.hasTagCompound()) {
            NBTTagCompound baseBlockNbt = item.getTagCompound().getCompoundTag("BaseBlock");
            ItemStack baseBlock = new ItemStack(baseBlockNbt);

            if (!baseBlock.isEmpty() && Block.getBlockFromItem(baseBlock.getItem()) != Blocks.AIR) {
                return baseBlock;
            }
        }

        return new ItemStack(Blocks.PLANKS);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(this.getBaseBlock(stack).getDisplayName());
    }

}