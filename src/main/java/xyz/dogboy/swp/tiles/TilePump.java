package xyz.dogboy.swp.tiles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import xyz.dogboy.swp.blocks.BlockWoodenVariation;
import xyz.dogboy.swp.config.SWPConfig;

public class TilePump extends PersistantSyncableTileEntity implements ITickable, IFluidHandler, WoodenVariationProvider {

    private int amount;

    @Override
    public void update() {
        BlockPos below = this.getPos().down();
        if (!this.isStaticWater(below)) {
            return;
        }

        int waterSources = 0;
        for (EnumFacing direction : EnumFacing.HORIZONTALS) {
            if (this.isStaticWater(below.offset(direction)))
                waterSources++;

            if (waterSources >= 2) {
                break;
            }
        }

        if (waterSources < 2) {
            return;
        }

        this.fillInternal(SWPConfig.pumpRate);

        if (this.amount > 0) {
            BlockPos offsetPos = this.getPos().offset(EnumFacing.UP);
            TileEntity tileEntity = this.getWorld().getTileEntity(offsetPos);
            if (tileEntity != null && tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.DOWN)) {
                IFluidHandler fluidHandler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.DOWN);
                if (fluidHandler != null) {
                    this.amount -= fluidHandler.fill(new FluidStack(FluidRegistry.WATER, Math.min(SWPConfig.transferRate, this.amount)), true);
                }
            }
        }
    }

    private boolean isStaticWater(BlockPos pos) {
        IBlockState blockState = this.getWorld().getBlockState(pos);
        return blockState.getBlock() instanceof BlockStaticLiquid && blockState.getMaterial() == Material.WATER;
    }

    private FluidStack getFluidStack() {
        if (this.amount <= 0) {
            return null;
        }

        return new FluidStack(FluidRegistry.WATER, this.amount);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && (facing == null || facing == EnumFacing.UP);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && (facing == null || facing == EnumFacing.UP)) {
            return (T) this;
        }

        return null;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[]{new FluidTankProperties(this.getFluidStack(), SWPConfig.internalVolume)};
    }

    private void fillInternal(int amount) {
        int maxFill = Math.min(SWPConfig.internalVolume - this.amount, amount);
        this.amount += maxFill;
        this.triggerUpdate();
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return 0;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource == null || resource.amount <= 0 || resource.getFluid() != FluidRegistry.WATER) {
            return null;
        }

        return this.drain(resource.amount, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (maxDrain <= 0) {
            return null;
        }

        FluidStack current = this.getFluidStack();
        if (current == null) {
            return null;
        }

        int drain = Math.min(SWPConfig.transferRate, Math.min(maxDrain, current.amount));

        if (!doDrain) {
            return new FluidStack(FluidRegistry.WATER, drain);
        }

        this.amount -= drain;
        this.triggerUpdate();
        return new FluidStack(FluidRegistry.WATER, drain);
    }

    @Override
    protected void writeData(NBTTagCompound tagCompound) {
        tagCompound.setInteger("FluidAmount", this.amount);
    }

    @Override
    protected void readData(NBTTagCompound tagCompound) {
        this.amount = tagCompound.getInteger("FluidAmount");
    }

    @Override
    public IExtendedBlockState writeExtendedState(IExtendedBlockState state) {
        TileEntity up = this.getWorld().getTileEntity(this.getPos().up());
        if (up instanceof WoodenVariationProvider) {
            return ((WoodenVariationProvider) up).writeExtendedState(state);
        }
        String texture = this.getTileData().getString("Texture");
        return state.withProperty(BlockWoodenVariation.TEXTURE, texture.isEmpty() ? "minecraft:blocks/planks_oak" : texture);
    }

}
