package xyz.dogboy.swp.client;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xyz.dogboy.swp.Utils;
import xyz.dogboy.swp.proxy.CommonProxy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class WoodenVariationItemOverrideList extends ItemOverrideList {
    public static final WoodenVariationItemOverrideList instance = new WoodenVariationItemOverrideList();

    private WoodenVariationItemOverrideList() {
        super(ImmutableList.of());
    }

    @Nonnull
    @Override
    public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
        ItemStack baseBlock;
        if (stack.hasTagCompound()) {
            baseBlock = new ItemStack(stack.getTagCompound().getCompoundTag("BaseBlock"));
        } else {
            baseBlock = CommonProxy.DEFAULT_MATERIAL.toItemStack();
        }
        if (!baseBlock.isEmpty()) {
            Block block = Block.getBlockFromItem(baseBlock.getItem());
            if (block != Blocks.AIR) {
                String texture = Utils.getTextureFromBlock(block, baseBlock.getItemDamage());
                return ((BakedWoodenVariationModel) originalModel).getActualModel(texture);
            }
        }
        return originalModel;
    }
}