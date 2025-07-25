package xyz.dogboy.swp.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import xyz.dogboy.swp.Utils;
import xyz.dogboy.swp.blocks.BlockPipe;

@SideOnly(Side.CLIENT)
public class BakedWoodenVariationModel extends BakedModelWrapper<IBakedModel> {
    private final Map<String, IBakedModel> cache = Maps.newHashMap();
    private final List<String> replacedTextures;
    private final IModel model;

    public BakedWoodenVariationModel(IBakedModel originalModel, IModel model, List<String> replacedTextures) {
        super(originalModel);
        this.model = model;
        this.replacedTextures = replacedTextures;
    }

    private TextureAtlasSprite getTexture(ResourceLocation resourceLocation) {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(resourceLocation.toString());
    }

    protected IBakedModel getActualModel(String texture) {
        IBakedModel bakedModel = this.originalModel;

        if (texture != null) {
            if (this.cache.containsKey(texture)) {
                bakedModel = this.cache.get(texture);
            } else if (this.model != null) {
                ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
                for (String key : this.replacedTextures) {
                    builder.put(key, texture);
                }

                IModel retextured = this.model.retexture(builder.build());
                IModelState modelState = retextured.getDefaultState();
                bakedModel = retextured.bake(modelState, DefaultVertexFormats.BLOCK, this::getTexture);
                this.cache.put(texture, bakedModel);
            }
        }
        return bakedModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (state instanceof IExtendedBlockState) {
            String texture = ((IExtendedBlockState) state).getValue(BlockPipe.TEXTURE);
            return this.getActualModel(texture).getQuads(state, side, rand);
        }

        return this.originalModel.getQuads(state, side, rand);
    }

    @Override
    public ItemOverrideList getOverrides() {
        return WoodenVariationItemOverrideList.instance;
    }
}
