package xyz.dogboy.swp.blocks;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xyz.dogboy.swp.Registry;
import xyz.dogboy.swp.client.DiggingParticle;
import xyz.dogboy.swp.config.CfgParser;
import xyz.dogboy.swp.config.SWPConfig;
import xyz.dogboy.swp.items.ItemBlockPipe;
import xyz.dogboy.swp.proxy.CommonProxy;
import xyz.dogboy.swp.tiles.TilePipe;

public class BlockPipe extends BlockWoodenVariation {

    private static ItemStack extractionUpgrade;

    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool DOWN = PropertyBool.create("down");

    public static final PropertyBool EXTRACTION = PropertyBool.create("extraction");
    public static final PropertyBool EXTRACT_NORTH = PropertyBool.create("extract_north");
    public static final PropertyBool EXTRACT_EAST = PropertyBool.create("extract_east");
    public static final PropertyBool EXTRACT_SOUTH = PropertyBool.create("extract_south");
    public static final PropertyBool EXTRACT_WEST = PropertyBool.create("extract_west");
    public static final PropertyBool EXTRACT_UP = PropertyBool.create("extract_up");
    public static final PropertyBool EXTRACT_DOWN = PropertyBool.create("extract_down");

    public static final PropertyBool STRAIGHT = PropertyBool.create("straight");

    public static final AxisAlignedBB MIDDLE_BB = new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
    public static final AxisAlignedBB NORTH_BB = new AxisAlignedBB(0.3125, 0.3125, 0, 0.6875, 0.6875, 0.25);
    public static final AxisAlignedBB EAST_BB = new AxisAlignedBB(1, 0.3125, 0.3125, 0.75, 0.6875, 0.6875);
    public static final AxisAlignedBB SOUTH_BB = new AxisAlignedBB(0.3125, 0.3125, 1, 0.6875, 0.6875, 0.75);
    public static final AxisAlignedBB WEST_BB = new AxisAlignedBB(0, 0.3125, 0.3125, 0.25, 0.6875, 0.6875);
    public static final AxisAlignedBB UP_BB = new AxisAlignedBB(0.3125, 1, 0.3125, 0.6875, 0.75, 0.6875);
    public static final AxisAlignedBB DOWN_BB = new AxisAlignedBB(0.3125, 0, 0.3125, 0.6875, 0.25, 0.6875);

    public static final List<ItemStack> stoneVariants = Collections.unmodifiableList(Arrays.asList(
            new ItemStack(Blocks.STONE, 1, 0),
            new ItemStack(Blocks.STONE, 1, 1),
            new ItemStack(Blocks.STONE, 1, 2),
            new ItemStack(Blocks.STONE, 1, 3),
            new ItemStack(Blocks.STONE, 1, 4),
            new ItemStack(Blocks.STONE, 1, 5),
            new ItemStack(Blocks.STONE, 1, 6)
    ));

    public BlockPipe() {
        super("pipe", Material.WOOD, MapColor.WOOD);
        setHardness(1.0F);
        setResistance(2.0F);
        setDefaultState(getBlockState().getBaseState()
                .withProperty(NORTH, false)
                .withProperty(EAST, false)
                .withProperty(SOUTH, false)
                .withProperty(WEST, false)
                .withProperty(UP, false)
                .withProperty(DOWN, false)
                .withProperty(EXTRACTION, false)
                .withProperty(EXTRACT_NORTH, false)
                .withProperty(EXTRACT_EAST, false)
                .withProperty(EXTRACT_SOUTH, false)
                .withProperty(EXTRACT_WEST, false)
                .withProperty(EXTRACT_UP, false)
                .withProperty(EXTRACT_DOWN, false)
                .withProperty(STRAIGHT, false));
    }

    @Override
    public void addCollisionBoxToList(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        if (!isActualState) {
            state = state.getActualState(worldIn, pos);
        }

        Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, MIDDLE_BB);

        if (state.getValue(NORTH))
            Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, NORTH_BB);

        if (state.getValue(EAST))
            Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, EAST_BB);

        if (state.getValue(SOUTH))
            Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, SOUTH_BB);

        if (state.getValue(WEST))
            Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, WEST_BB);

        if (state.getValue(UP))
            Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, UP_BB);

        if (state.getValue(DOWN))
            Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, DOWN_BB);
    }

    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        state = this.getActualState(state, source, pos);

        AxisAlignedBB boundingBox = MIDDLE_BB;

        if (state.getValue(NORTH))
            boundingBox = boundingBox.union(NORTH_BB);

        if (state.getValue(EAST))
            boundingBox = boundingBox.union(EAST_BB);

        if (state.getValue(SOUTH))
            boundingBox = boundingBox.union(SOUTH_BB);

        if (state.getValue(WEST))
            boundingBox = boundingBox.union(WEST_BB);

        if (state.getValue(UP))
            boundingBox = boundingBox.union(UP_BB);

        if (state.getValue(DOWN))
            boundingBox = boundingBox.union(DOWN_BB);

        return boundingBox;
    }

    @Override
    public boolean onBlockActivated(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        TilePipe pipe = (TilePipe) worldIn.getTileEntity(pos);
        if (pipe == null) {
            return false;
        }

        IFluidHandlerItem fluidHandler = playerIn.getHeldItem(hand).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandler != null) {
            return this.handleFluidHandlerActivate(playerIn, hand, pipe, fluidHandler);
        }

        ItemStack extractUpgrade = BlockPipe.getExtractionUpgrade();

        if (BlockPipe.areItemStacksEqual(playerIn.getHeldItem(hand), extractUpgrade) || playerIn.isSneaking()) {
            return this.handleExtractUpgradeActivate(playerIn, hand, pipe);
        }

        return false;
    }

    @Nonnull
    @Override
    public SoundType getSoundType(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nullable Entity entity) {
        return getBaseBlockState(world, pos).getBlock().getSoundType(state, world, pos, entity);
    }

    @Override
    public boolean addLandingEffects(@Nonnull IBlockState state, @Nonnull WorldServer worldObj, @Nonnull BlockPos blockPosition, @Nonnull IBlockState iblockstate, @Nonnull EntityLivingBase entity, int numberOfParticles) {
        if (worldObj.getBlockState(blockPosition).getBlock() instanceof BlockPipe) {
            worldObj.spawnParticle(EnumParticleTypes.BLOCK_CRACK, blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), numberOfParticles, -0.5D, 0.1D, -0.5D, 0.15D, Block.getStateId(getBaseBlockState(worldObj, blockPosition)));
        }
        return true;
    }

    @Override
    public boolean addRunningEffects(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Entity entity) {
        if (world.getBlockState(pos).getBlock() instanceof BlockPipe) {
            int i = pos.getX();
            int k = pos.getZ();
            float f = 0.1F;
            AxisAlignedBB axisalignedbb = state.getBoundingBox(world, pos);
            double x = (double) i + world.rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 2 * f) + f + axisalignedbb.minX;
            double z = (double) k + world.rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 2 * f) + f + axisalignedbb.minZ;

            world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, x, entity.getEntityBoundingBox().minY + 0.1D, z, -entity.motionX * 4.0D, 1.5D, -entity.motionZ * 4.0D, Block.getStateId(getBaseBlockState(world, pos)));
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addHitEffects(@Nonnull IBlockState state, @Nonnull World worldObj, @Nonnull RayTraceResult target, @Nonnull ParticleManager manager) {
        if (worldObj.getBlockState(target.getBlockPos()).getBlock() instanceof BlockPipe) {
            int i = target.getBlockPos().getX();
            int j = target.getBlockPos().getY();
            int k = target.getBlockPos().getZ();
            float f = 0.1F;
            AxisAlignedBB axisalignedbb = state.getBoundingBox(worldObj, target.getBlockPos());
            double d0 = (double) i + worldObj.rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 2 * f) + f + axisalignedbb.minX;
            double d1 = (double) j + worldObj.rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 2 * f) + f + axisalignedbb.minY;
            double d2 = (double) k + worldObj.rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 2 * f) + f + axisalignedbb.minZ;

            switch (target.sideHit) {
                case DOWN:
                    d1 = (double) j + axisalignedbb.minY - f;
                    break;
                case UP:
                    d1 = (double) j + axisalignedbb.maxY + f;
                    break;
                case NORTH:
                    d2 = (double) k + axisalignedbb.minZ - f;
                    break;
                case SOUTH:
                    d2 = (double) k + axisalignedbb.maxZ + f;
                    break;
                case WEST:
                    d0 = (double) i + axisalignedbb.minX - f;
                    break;
                case EAST:
                    d0 = (double) i + axisalignedbb.maxX + f;
                    break;
            }
            manager.addEffect((new DiggingParticle(worldObj, d0, d1, d2, 0.0D, 0.0D, 0.0D, getBaseBlockState(worldObj, target.getBlockPos()))).setBlockPos(target.getBlockPos()).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addDestroyEffects(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull ParticleManager manager) {
        if (world.getBlockState(pos).getBlock() instanceof BlockPipe) {
            manager.addBlockDestroyEffects(pos, getBaseBlockState(world, pos));
        }
        return true;
    }

    private IBlockState getBaseBlockState(World world, BlockPos pos) {
        ItemBlockPipe itemBlockPipe = (ItemBlockPipe) this.getItem(world, pos, world.getBlockState(pos)).getItem();
        ItemStack baseBlockItem = itemBlockPipe.getBaseBlock(this.getItem(world, pos, world.getBlockState(pos)));
        return Block.getBlockFromItem(baseBlockItem.getItem()).getStateFromMeta(baseBlockItem.getMetadata());
    }

    private boolean handleFluidHandlerActivate(EntityPlayer playerIn, EnumHand hand, TilePipe pipe, IFluidHandlerItem fluidHandler) {
        IFluidTankProperties tankProperties = pipe.getTankProperties()[0];
        int maxDrain = tankProperties.getCapacity() - (tankProperties.getContents() == null ? 0 : tankProperties.getContents().amount);
        if (maxDrain <= 0) {
            return false;
        }

        FluidStack drained = fluidHandler.drain(maxDrain, false);
        if (drained == null) {
            return false;
        }

        maxDrain = pipe.fill(drained, false);
        if (maxDrain <= 0) {
            return false;
        }

        drained = fluidHandler.drain(maxDrain, true);
        if (drained == null) {
            return false;
        }

        pipe.fill(drained, true);
        playerIn.setHeldItem(hand, fluidHandler.getContainer());

        return true;
    }

    private boolean handleExtractUpgradeActivate(EntityPlayer playerIn, EnumHand hand, TilePipe pipe) {
        if (playerIn.isSneaking()) {
            if (!pipe.isExtractionEnabled()) {
                return false;
            }

            pipe.setExtractionEnabled(false);

            if (!playerIn.capabilities.isCreativeMode) {
                ItemStack extractUpgrade = BlockPipe.getExtractionUpgrade();
                if (!playerIn.addItemStackToInventory(extractUpgrade)) {
                    playerIn.dropItem(extractUpgrade, false);
                }
            }

            playerIn.playSound(SoundEvents.BLOCK_ANVIL_PLACE, 1, 1);
        } else {
            if (pipe.isExtractionEnabled()) {
                return false;
            }

            pipe.setExtractionEnabled(true);

            if (!playerIn.capabilities.isCreativeMode) {
                playerIn.getHeldItem(hand).shrink(1);
            }

            playerIn.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }

        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
            NBTTagCompound baseBlockNbt = new NBTTagCompound();
            CommonProxy.DEFAULT_MATERIAL.toItemStack().writeToNBT(baseBlockNbt);
            stack.getTagCompound().setTag("BaseBlock", baseBlockNbt);
        }

        NBTTagCompound baseBlock = stack.getTagCompound().getCompoundTag("BaseBlock");
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TilePipe) {
            tileEntity.getTileData().setTag("BaseBlock", baseBlock);
        }
    }

    @Override
    public boolean hasTileEntity(@Nonnull IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TilePipe();
    }

    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(@Nonnull IBlockState state) {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered(@Nonnull IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        return true;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new IProperty[]{NORTH, EAST, SOUTH, WEST, UP, DOWN, EXTRACTION, EXTRACT_NORTH, EXTRACT_EAST, EXTRACT_SOUTH, EXTRACT_WEST, EXTRACT_UP, EXTRACT_DOWN, STRAIGHT},
                new IUnlistedProperty[]{BlockWoodenVariation.TEXTURE});
    }

    public boolean canConnectTo(IBlockAccess world, BlockPos pipePos, EnumFacing direction, boolean excludePipe) {
        TileEntity pipeTileEntity = world.getTileEntity(pipePos);
        if (pipeTileEntity instanceof TilePipe) {
            return ((TilePipe) pipeTileEntity).canConnectTo(direction, excludePipe);
        }

        return false;
    }

    public int getMetaFromState(@Nonnull IBlockState state) {
        return 0;
    }

    @Nonnull
    @Override
    public IBlockState getActualState(IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        TileEntity tileentity = worldIn instanceof ChunkCache
                ? ((ChunkCache) worldIn).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK)
                : worldIn.getTileEntity(pos);
        boolean extraction = tileentity instanceof TilePipe && ((TilePipe) tileentity).isExtractionEnabled();

        boolean north = this.canConnectTo(worldIn, pos, EnumFacing.NORTH, false);
        boolean east = this.canConnectTo(worldIn, pos, EnumFacing.EAST, false);
        boolean south = this.canConnectTo(worldIn, pos, EnumFacing.SOUTH, false);
        boolean west = this.canConnectTo(worldIn, pos, EnumFacing.WEST, false);
        boolean up = this.canConnectTo(worldIn, pos, EnumFacing.UP, false);
        boolean down = this.canConnectTo(worldIn, pos, EnumFacing.DOWN, false);

        boolean straight = (north && south && !east && !west && !up && !down) || (east && west && !north && !south && !up && !down) || (up && down && !north && !south && !east && !west);

        return state.withProperty(NORTH, this.canConnectTo(worldIn, pos, EnumFacing.NORTH, false))
                .withProperty(EAST, this.canConnectTo(worldIn, pos, EnumFacing.EAST, false))
                .withProperty(SOUTH, this.canConnectTo(worldIn, pos, EnumFacing.SOUTH, false))
                .withProperty(WEST, this.canConnectTo(worldIn, pos, EnumFacing.WEST, false))
                .withProperty(UP, this.canConnectTo(worldIn, pos, EnumFacing.UP, false))
                .withProperty(DOWN, this.canConnectTo(worldIn, pos, EnumFacing.DOWN, false))
                .withProperty(EXTRACTION, extraction)
                .withProperty(EXTRACT_NORTH, extraction && this.canConnectTo(worldIn, pos, EnumFacing.NORTH, true))
                .withProperty(EXTRACT_EAST, extraction && this.canConnectTo(worldIn, pos, EnumFacing.EAST, true))
                .withProperty(EXTRACT_SOUTH, extraction && this.canConnectTo(worldIn, pos, EnumFacing.SOUTH, true))
                .withProperty(EXTRACT_WEST, extraction && this.canConnectTo(worldIn, pos, EnumFacing.WEST, true))
                .withProperty(EXTRACT_UP, extraction && this.canConnectTo(worldIn, pos, EnumFacing.UP, true))
                .withProperty(EXTRACT_DOWN, extraction && this.canConnectTo(worldIn, pos, EnumFacing.DOWN, true))
                .withProperty(STRAIGHT, straight);
    }

    public ItemStack getItem(IBlockAccess world, BlockPos pos) {
        ItemStack itemStack = new ItemStack(Registry.PIPE_ITEM);
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TilePipe) {
            itemStack.setTagCompound(new NBTTagCompound());
            itemStack.getTagCompound().setTag("BaseBlock", tileEntity.getTileData().getCompoundTag("BaseBlock").copy());
        }
        return itemStack;
    }

    @Nonnull
    @Override
    public ItemStack getItem(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        return this.getItem(worldIn, pos);
    }

    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
        if (willHarvest) {
            return true;
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void harvestBlock(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable TileEntity te, @Nonnull ItemStack tool) {
        super.harvestBlock(world, player, pos, state, te, tool);
        world.setBlockToAir(pos);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
        drops.add(this.getItem(world, pos));

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TilePipe && ((TilePipe) tileEntity).isExtractionEnabled()) {
            ItemStack extractUpgrade = BlockPipe.getExtractionUpgrade();
            drops.add(extractUpgrade);
        }
    }

    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return BlockFaceShape.CENTER;
    }

    public static ItemStack getExtractionUpgrade() {
        if (BlockPipe.extractionUpgrade == null) {
            try {
                CfgParser.ConfigItem configItem = new CfgParser.ConfigItem(SWPConfig.pipeExtractionItem);
                BlockPipe.extractionUpgrade = configItem.toItemStack();
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse SWP pipe extraction upgrade item", e);
            }
        }
        return BlockPipe.extractionUpgrade.copy();
    }

    private static boolean areItemStacksEqual(ItemStack stackA, ItemStack stackB) {
        if (stackA.getItem() != stackB.getItem()) {
            return false;
        } else if (stackA.getItemDamage() != stackB.getItemDamage()) {
            return false;
        } else if (stackA.getTagCompound() == null && stackB.getTagCompound() != null) {
            return false;
        }

        return (stackA.getTagCompound() == null || stackA.getTagCompound().equals(stackB.getTagCompound())) && stackA.areCapsCompatible(stackB);
    }

}
