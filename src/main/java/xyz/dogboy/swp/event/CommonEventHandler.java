package xyz.dogboy.swp.event;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreIngredient;
import xyz.dogboy.simplewoodenpipes.Tags;
import xyz.dogboy.swp.Registry;
import xyz.dogboy.swp.Utils;
import xyz.dogboy.swp.blocks.BlockPipe;
import xyz.dogboy.swp.config.SWPConfig;
import xyz.dogboy.swp.items.ItemBlockWoodenVariation;
import xyz.dogboy.swp.tiles.TilePipe;
import xyz.dogboy.swp.tiles.TilePump;

import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class CommonEventHandler {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(Registry.PIPE, Registry.PUMP);

        GameRegistry.registerTileEntity(TilePipe.class, Registry.PIPE.getRegistryName());
        GameRegistry.registerTileEntity(TilePump.class, Registry.PUMP.getRegistryName());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(Registry.PIPE_ITEM, Registry.PUMP_ITEM);
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        if (!SWPConfig.addRecipes) return;
        Stream.concat(Utils.getAllPlanks().stream(), BlockPipe.stoneVariants.stream())
                .map(baseBlock -> {
                    Ingredient baseBlockIngredient = Ingredient.fromStacks(baseBlock);
                    Ingredient glassIngredient = new OreIngredient("blockGlass");

                    ItemStack output = ((ItemBlockWoodenVariation) Registry.PIPE_ITEM).getWithBaseBlock(baseBlock);
                    output.setCount(6);

                    return getRecipe(
                            String.format("pipe_%s_%s_%d", baseBlock.getItem().getRegistryName().getNamespace(),
                                    baseBlock.getItem().getRegistryName().getNamespace(), baseBlock.getMetadata()),
                            output,

                            baseBlockIngredient, glassIngredient, baseBlockIngredient,
                            baseBlockIngredient, glassIngredient, baseBlockIngredient,
                            baseBlockIngredient, glassIngredient, baseBlockIngredient
                    );
                })
                .forEach(event.getRegistry()::register);
    }

    private static IRecipe getRecipe(String id, ItemStack output, Ingredient... ingredients) {
        ShapedRecipes recipe = new ShapedRecipes("", 3, 3, NonNullList.from(ingredients[0], ingredients), output);
        return recipe.setRegistryName(new ResourceLocation(Tags.MOD_ID, id));
    }
}
