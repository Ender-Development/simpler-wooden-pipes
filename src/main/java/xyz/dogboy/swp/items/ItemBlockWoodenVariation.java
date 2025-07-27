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
import xyz.dogboy.simplewoodenpipes.Tags;
import xyz.dogboy.swp.Utils;
import xyz.dogboy.swp.config.SWPConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

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

    @Nonnull
    @Override
    public String getTranslationKey() {
        return SWPConfig.advancedNamingScheme ? this.getTranslationKey(new ItemStack(this)) : super.getTranslationKey();
    }

    @Nonnull
    @Override
    public String getTranslationKey(@Nonnull ItemStack stack) {
        String advancedName = String.format("tile.%s.%s_%s", Tags.MOD_ID, this.getBlock().getRegistryName().getPath(), this.getBaseBlock(stack).getTranslationKey().toLowerCase(Locale.ROOT).replace("tile.", "").replace(".name", ""));
        return SWPConfig.advancedNamingScheme ? advancedName : super.getTranslationKey(stack);
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
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (!SWPConfig.advancedNamingScheme) {
            tooltip.add(this.getBaseBlock(stack).getDisplayName());
        }
    }
}