package xyz.dogboy.swp.event;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xyz.dogboy.simplewoodenpipes.Tags;
import xyz.dogboy.swp.Registry;
import xyz.dogboy.swp.SimpleWoodenPipes;
import xyz.dogboy.swp.client.BakedWoodenVariationModel;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class ClientEventHandler {
    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(Registry.PIPE_ITEM, 0, new ModelResourceLocation(Registry.PIPE_ITEM.getRegistryName(), "normal"));
        ModelLoader.setCustomModelResourceLocation(Registry.PUMP_ITEM, 0, new ModelResourceLocation(Registry.PUMP_ITEM.getRegistryName(), "inventory"));
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        for (ModelResourceLocation model : event.getModelRegistry().getKeys()) {
            if (!model.getNamespace().equals(Tags.MOD_ID)) {
                continue;
            }

            if (model.getPath().equals("pipe")) {
                replaceWoodenVariationModel(model, Arrays.asList("main", "particle"), event);
            } else if (model.getPath().equals("pump")) {
                replaceWoodenVariationModel(model, Collections.singletonList("pipe"), event);
            }
        }
    }

    private void replaceWoodenVariationModel(ModelResourceLocation modelLocation, List<String> replaceTextures, ModelBakeEvent event) {
        try {
            IModel model = ModelLoaderRegistry.getModel(modelLocation);
            IBakedModel baseModel = event.getModelRegistry().getObject(modelLocation);
            IBakedModel modifiedModel = new BakedWoodenVariationModel(baseModel, model, replaceTextures);
            event.getModelRegistry().putObject(modelLocation, modifiedModel);
        } catch (Exception e) {
            SimpleWoodenPipes.LOGGER.error("Failed to replace model for: {} with exception {}", modelLocation, e);
        }
    }
}
