package xyz.dogboy.swp.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

import xyz.dogboy.simplewoodenpipes.Tags;
import xyz.dogboy.swp.Registry;

public class BlockBase extends Block {

    public BlockBase(String name, Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
        setTranslationKey(String.format("%s.%s", Tags.MOD_ID, name));
        setRegistryName(new ResourceLocation(Tags.MOD_ID, name));
        setCreativeTab(Registry.CREATIVETAB);
    }
}
