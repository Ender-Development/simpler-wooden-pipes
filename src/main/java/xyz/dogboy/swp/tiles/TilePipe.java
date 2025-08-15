package xyz.dogboy.swp.tiles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import xyz.dogboy.swp.utils.PipePriorityMap;
import xyz.dogboy.swp.utils.Utils;
import xyz.dogboy.swp.blocks.BlockWoodenVariation;
import xyz.dogboy.swp.config.SWPConfig;

import java.util.ArrayList;

public class TilePipe extends PersistantSyncableTileEntity implements ITickable, WoodenVariationProvider {
    public static final int PRIORITY_BLOCK = -1000;
    public static final int PRIORITY_PIPE = 0;

    private ItemStack cachedBaseBlock;
    private FluidTank tank;
    private EnumFacing lastTransfer;

    private final boolean[] from = new boolean[EnumFacing.VALUES.length];
    private boolean extraction;
    private boolean clogged = false;

    private boolean syncTank;
    private boolean syncCloggedFlag;
    private boolean syncTransfer;

    private int lastRobin;
    private int ticksSinceLastUpdate;

    public TilePipe() {
        initFluidTank();
    }

    protected void initFluidTank() {
        tank = new FluidTank(SWPConfig.internalVolume) {
            @Override
            protected void onContentsChanged() {
                markDirty();
            }
        };
    }

    @Nonnull
    public FluidTank getTank() {
        return tank;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        // minor optimization to avoid sending updates if nothing changed
        if (requiresSync()) {
            NBTTagCompound updateTag = getSyncTag();
            resetSync();
            return new SPacketUpdateTileEntity(getPos(), 0, updateTag);
        }
        return null;
    }

    @Override
    public void update() {
        if (this.getWorld().isRemote) {
            return;
        }

        if (this.tank.getFluid() != null && this.tank.getFluid().getFluid().getTemperature() >= 550 && Utils.isBurnable(this.getBaseBlock())) {
            this.getWorld().setBlockState(this.getPos(), Blocks.FIRE.getDefaultState());
            return;
        }

        ticksSinceLastUpdate++;
        boolean fluidMoved = false;

        // Push fluid to connected handlers
        FluidStack passStack = this.tank.drain(SWPConfig.transferRate, false);
        if (passStack != null) {
            PipePriorityMap<Integer, EnumFacing> possibleDirections = new PipePriorityMap<>();
            IFluidHandler[] fluidHandlers = new IFluidHandler[EnumFacing.VALUES.length];

            for (EnumFacing facing : EnumFacing.values()) {
                if (!isConnected(facing) || isFrom(facing)) {
                    continue;
                }
                TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
                if (tileEntity != null && tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite())) {
                    IFluidHandler fluidHandler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
                    int priority = PRIORITY_BLOCK;
                    if (tileEntity instanceof TilePump) {
                        continue;
                    }
                    if (tileEntity instanceof TilePipe) {
                        priority = ((TilePipe) tileEntity).getPriority(facing.getOpposite());
                    }
                    if (isFrom(facing.getOpposite())) {
                        priority -= 20;
                    }
                    if (this.isExtractionEnabled() && !(tileEntity instanceof TilePipe)) {
                        priority = Integer.MAX_VALUE;
                    }
                    possibleDirections.put(priority, facing);
                    fluidHandlers[facing.getIndex()] = fluidHandler;
                }
            }

            for (int key : possibleDirections.keySet()) {
                ArrayList<EnumFacing> list = possibleDirections.get(key);
                for (int i = 0; i < list.size(); i++) {
                    EnumFacing facing = list.get((i + lastRobin) % list.size());
                    IFluidHandler handler = fluidHandlers[facing.getIndex()];
                    fluidMoved = pushStack(passStack, facing, handler);
                    if (lastTransfer != facing) {
                        syncTransfer = true;
                        lastTransfer = facing;
                        markDirty();
                    }
                    if (fluidMoved) {
                        lastRobin++;
                        break;
                    }
                }
                if (fluidMoved)
                    break;
            }
        }
        // Try to pump fluid from connected handlers
        if (this.tank.canFill() && isExtractionEnabled()) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (!isConnected(facing) || isFrom(facing)) {
                    continue;
                }
                TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
                if (tileEntity == null || tileEntity instanceof TilePump || tileEntity instanceof TilePipe) {
                    continue;
                }
                if (tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite())) {
                    IFluidHandler fluidHandler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
                    if (fluidHandler != null && pumpStack(fluidHandler)) {
                        fluidMoved = true;
                        markDirty();
                        break;
                    }
                }
            }
        }
        // Handle fluid transfer state
        if (this.tank.getFluidAmount() <= 0) {
            if (lastTransfer != null && !fluidMoved) {
                syncTransfer = true;
                lastTransfer = null;
                markDirty();
            }
            fluidMoved = true;
            if (ticksSinceLastUpdate > 20) {
                ticksSinceLastUpdate = 0;
                resetFrom();
            }
        }
        if (clogged == fluidMoved) {
            clogged = !fluidMoved;
            syncCloggedFlag = true;
            markDirty();
        }
    }

    public int getPriority(EnumFacing facing) {
        return PRIORITY_PIPE;
    }

    private boolean isConnected(EnumFacing facing) {
        BlockPos pos = this.getPos().offset(facing);
        TileEntity tileEntity = this.getWorld().getTileEntity(pos);
        return this.canConnectTo(tileEntity, facing, false);
    }

    private boolean isFrom(EnumFacing facing) {
        return this.from[facing.getIndex()];
    }

    private void resetFrom() {
        for (EnumFacing facing : EnumFacing.VALUES) {
            setFrom(facing, false);
        }
    }

    private void setFrom(EnumFacing facing, boolean flag) {
        from[facing.getIndex()] = flag;
    }

    private boolean pushStack(FluidStack passStack, EnumFacing facing, IFluidHandler handler) {
        int added = handler.fill(passStack, false);
        if (added > 0) {
            handler.fill(passStack, true);
            this.tank.drain(added, true);
            passStack.amount -= added;
            return passStack.amount <= 0;
        }

        if (isFrom(facing))
            setFrom(facing, true);
        return false;
    }

    private boolean pumpStack(IFluidHandler fluidHandler) {
        int drainAmount = Math.min(SWPConfig.transferRate, this.tank.getCapacity() - this.tank.getFluidAmount());
        FluidStack drained = fluidHandler.drain(drainAmount, false);
        if (drained != null && drained.amount > 0) {
            if (this.tank.fill(drained, false) > 0) {
                int filled = this.tank.fill(drained, true);
                fluidHandler.drain(filled, true);
                return true;
            }
        }
        return false;
    }

    public boolean isExtractionEnabled() {
        return this.extraction;
    }

    public void setExtractionEnabled(boolean extraction) {
        this.extraction = extraction;
        this.triggerUpdate();
    }

    protected void resetSync() {
        syncTank = false;
        syncCloggedFlag = false;
        syncTransfer = false;
    }

    protected boolean requiresSync() {
        return syncTank || syncCloggedFlag || syncTransfer;
    }

    protected NBTTagCompound getSyncTag() {
        NBTTagCompound compound = new NBTTagCompound();
        if (syncTank)
            writeTank(compound);
        if (syncCloggedFlag)
            writeCloggedFlag(compound);
        if (syncTransfer)
            writeLastTransfer(compound);
        return compound;
    }

    @Override
    public void writeData(@Nonnull NBTTagCompound tag) {
        writeTank(tag);
        writeCloggedFlag(tag);
        writeLastTransfer(tag);
        writeExtraction(tag);
        for (EnumFacing facing : EnumFacing.VALUES)
            tag.setBoolean("from" + facing.getIndex(), from[facing.getIndex()]);
        tag.setInteger("lastRobin", lastRobin);
    }

    private void writeCloggedFlag(NBTTagCompound tag) {
        tag.setBoolean("clogged", clogged);
    }

    private void writeLastTransfer(NBTTagCompound tag) {
        tag.setInteger("lastTransfer", Utils.writeNullableFacing(lastTransfer));
    }

    private void writeTank(NBTTagCompound tag) {
        tag.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
    }

    private void writeExtraction(NBTTagCompound tag) {
        tag.setBoolean("CanExtract", extraction);
    }

    @Override
    public void readData(@Nonnull NBTTagCompound tag) {
        if (tag.hasKey("clogged"))
            clogged = tag.getBoolean("clogged");
        if (tag.hasKey("tank"))
            tank.readFromNBT(tag.getCompoundTag("tank"));
        if (tag.hasKey("lastTransfer"))
            lastTransfer = Utils.readNullableFacing(tag.getInteger("lastTransfer"));
        for (EnumFacing facing : EnumFacing.VALUES)
            if (tag.hasKey("from" + facing.getIndex()))
                from[facing.getIndex()] = tag.getBoolean("from" + facing.getIndex());
        if (tag.hasKey("lastRobin"))
            lastRobin = tag.getInteger("lastRobin");
        if (tag.hasKey("CanExtract")) {
            this.extraction = tag.getBoolean("CanExtract");
        }
    }

    public ItemStack getBaseBlock() {
        if (this.cachedBaseBlock == null) {
            if (!this.getTileData().hasKey("BaseBlock")) {
                return new ItemStack(Blocks.AIR);
            }
            NBTTagCompound baseBlock = this.getTileData().getCompoundTag("BaseBlock");
            this.cachedBaseBlock = new ItemStack(baseBlock);
        }
        return this.cachedBaseBlock;
    }

    public IExtendedBlockState writeExtendedState(IExtendedBlockState state) {
        String texture = this.getTileData().getString("Texture");

        if (texture.isEmpty()) {
            ItemStack stack = this.getBaseBlock();
            if (!stack.isEmpty()) {
                Block block = Block.getBlockFromItem(stack.getItem());
                if (block != Blocks.AIR) {
                    texture = Utils.getTextureFromBlock(block, stack.getItemDamage());
                    this.getTileData().setString("Texture", texture);
                }
            }
        }
        return state.withProperty(BlockWoodenVariation.TEXTURE, texture.isEmpty() ? "minecraft:blocks/planks_oak" : texture);
    }

    public boolean canConnectTo(EnumFacing direction, boolean excludePipe) {
        return this.canConnectTo(this.getWorld().getTileEntity(this.getPos().offset(direction)), direction, excludePipe);
    }

    public boolean canConnectTo(TileEntity tileEntity, EnumFacing direction, boolean excludePipe) {
        if (tileEntity == null) {
            return false;
        }
        if (tileEntity instanceof TilePump) {
            return direction == EnumFacing.DOWN && !excludePipe;
        }
        if (tileEntity instanceof TilePipe) {
            if (excludePipe) {
                return false;
            }
            TilePipe otherPipe = (TilePipe) tileEntity;
            if (SWPConfig.variantInterconnection) {
                return Utils.isBurnable(otherPipe.getBaseBlock()) == Utils.isBurnable(this.getBaseBlock());
            }
            return otherPipe.getBaseBlock().isItemEqual(this.getBaseBlock());
        }
        return tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite());
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) this.getTank();
        }
        return null;
    }
}
