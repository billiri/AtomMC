package net.minecraft.block;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.bukkit.event.block.BlockRedstoneEvent;

public class BlockTrapDoor extends Block {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyBool OPEN = PropertyBool.create("open");
    public static final PropertyEnum<DoorHalf> HALF = PropertyEnum.<DoorHalf>create("half", DoorHalf.class);
    protected static final AxisAlignedBB EAST_OPEN_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.1875D, 1.0D, 1.0D);
    protected static final AxisAlignedBB WEST_OPEN_AABB = new AxisAlignedBB(0.8125D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB SOUTH_OPEN_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.1875D);
    protected static final AxisAlignedBB NORTH_OPEN_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.8125D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB BOTTOM_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.1875D, 1.0D);
    protected static final AxisAlignedBB TOP_AABB = new AxisAlignedBB(0.0D, 0.8125D, 0.0D, 1.0D, 1.0D, 1.0D);

    protected BlockTrapDoor(Material materialIn) {
        super(materialIn);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(OPEN, Boolean.valueOf(false)).withProperty(HALF, DoorHalf.BOTTOM));
        this.setCreativeTab(CreativeTabs.REDSTONE);
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        AxisAlignedBB axisalignedbb;

        if (((Boolean) state.getValue(OPEN)).booleanValue()) {
            switch ((EnumFacing) state.getValue(FACING)) {
                case NORTH:
                default:
                    axisalignedbb = NORTH_OPEN_AABB;
                    break;
                case SOUTH:
                    axisalignedbb = SOUTH_OPEN_AABB;
                    break;
                case WEST:
                    axisalignedbb = WEST_OPEN_AABB;
                    break;
                case EAST:
                    axisalignedbb = EAST_OPEN_AABB;
            }
        } else if (state.getValue(HALF) == DoorHalf.TOP) {
            axisalignedbb = TOP_AABB;
        } else {
            axisalignedbb = BOTTOM_AABB;
        }

        return axisalignedbb;
    }

    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    public boolean isFullCube(IBlockState state) {
        return false;
    }

    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return !((Boolean) worldIn.getBlockState(pos).getValue(OPEN)).booleanValue();
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (this.blockMaterial == Material.IRON) {
            return false;
        } else {
            state = state.cycleProperty(OPEN);
            worldIn.setBlockState(pos, state, 2);
            this.playSound(playerIn, worldIn, pos, ((Boolean) state.getValue(OPEN)).booleanValue());
            return true;
        }
    }

    protected void playSound(@Nullable EntityPlayer player, World worldIn, BlockPos pos, boolean p_185731_4_) {
        if (p_185731_4_) {
            int i = this.blockMaterial == Material.IRON ? 1037 : 1007;
            worldIn.playEvent(player, i, pos, 0);
        } else {
            int j = this.blockMaterial == Material.IRON ? 1036 : 1013;
            worldIn.playEvent(player, j, pos, 0);
        }
    }

    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote) {
            boolean flag = worldIn.isBlockPowered(pos);

            if (flag || blockIn.getDefaultState().canProvidePower()) {
                org.bukkit.World bworld = worldIn.getWorld();
                org.bukkit.block.Block bblock = bworld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());

                int power = bblock.getBlockPower();
                int oldPower = state.getValue(OPEN) ? 15 : 0;

                if (oldPower == 0 ^ power == 0 || blockIn.getDefaultState().hasComparatorInputOverride()) {
                    BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(bblock, oldPower, power);
                    worldIn.getServer().getPluginManager().callEvent(eventRedstone);
                    flag = eventRedstone.getNewCurrent() > 0;
                }

                boolean flag1 = ((Boolean) state.getValue(OPEN)).booleanValue();

                if (flag1 != flag) {
                    worldIn.setBlockState(pos, state.withProperty(OPEN, Boolean.valueOf(flag)), 2);
                    this.playSound((EntityPlayer) null, worldIn, pos, flag);
                }
            }
        }
    }

    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        IBlockState iblockstate = this.getDefaultState();

        if (facing.getAxis().isHorizontal()) {
            iblockstate = iblockstate.withProperty(FACING, facing).withProperty(OPEN, Boolean.valueOf(false));
            iblockstate = iblockstate.withProperty(HALF, hitY > 0.5F ? DoorHalf.TOP : DoorHalf.BOTTOM);
        } else {
            iblockstate = iblockstate.withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(OPEN, Boolean.valueOf(false));
            iblockstate = iblockstate.withProperty(HALF, facing == EnumFacing.UP ? DoorHalf.BOTTOM : DoorHalf.TOP);
        }

        if (worldIn.isBlockPowered(pos)) {
            iblockstate = iblockstate.withProperty(OPEN, Boolean.valueOf(true));
        }

        return iblockstate;
    }

    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        return true;
    }

    protected static EnumFacing getFacing(int meta) {
        switch (meta & 3) {
            case 0:
                return EnumFacing.NORTH;
            case 1:
                return EnumFacing.SOUTH;
            case 2:
                return EnumFacing.WEST;
            case 3:
            default:
                return EnumFacing.EAST;
        }
    }

    protected static int getMetaForFacing(EnumFacing facing) {
        switch (facing) {
            case NORTH:
                return 0;
            case SOUTH:
                return 1;
            case WEST:
                return 2;
            case EAST:
            default:
                return 3;
        }
    }

    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, getFacing(meta)).withProperty(OPEN, Boolean.valueOf((meta & 4) != 0)).withProperty(HALF, (meta & 8) == 0 ? DoorHalf.BOTTOM : DoorHalf.TOP);
    }

    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i = i | getMetaForFacing((EnumFacing) state.getValue(FACING));

        if (((Boolean) state.getValue(OPEN)).booleanValue()) {
            i |= 4;
        }

        if (state.getValue(HALF) == DoorHalf.TOP) {
            i |= 8;
        }

        return i;
    }

    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
    }

    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(FACING)));
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING, OPEN, HALF});
    }

    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return (face == EnumFacing.UP && state.getValue(HALF) == DoorHalf.TOP || face == EnumFacing.DOWN && state.getValue(HALF) == DoorHalf.BOTTOM) && !((Boolean) state.getValue(OPEN)).booleanValue() ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        if (state.getValue(OPEN)) {
            IBlockState down = world.getBlockState(pos.down());
            if (down.getBlock() == net.minecraft.init.Blocks.LADDER)
                return down.getValue(BlockLadder.FACING) == state.getValue(FACING);
        }
        return false;
    }

    public static enum DoorHalf implements IStringSerializable {
        TOP("top"),
        BOTTOM("bottom");

        private final String name;

        private DoorHalf(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }
    }
}