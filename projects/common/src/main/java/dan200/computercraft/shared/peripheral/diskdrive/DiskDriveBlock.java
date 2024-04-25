// Copyright Daniel Ratcliffe, 2011-2022. Do not distribute without permission.
//
// SPDX-License-Identifier: LicenseRef-CCPL

package dan200.computercraft.shared.peripheral.diskdrive;

import com.mojang.serialization.MapCodec;
import dan200.computercraft.impl.MediaProviders;
import dan200.computercraft.shared.ModRegistry;
import dan200.computercraft.shared.common.HorizontalContainerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class DiskDriveBlock extends HorizontalContainerBlock {
    private static final MapCodec<DiskDriveBlock> CODEC = simpleCodec(DiskDriveBlock::new);

    public static final EnumProperty<DiskDriveState> STATE = EnumProperty.create("state", DiskDriveState.class);

    private static final BlockEntityTicker<DiskDriveBlockEntity> serverTicker = (level, pos, state, drive) -> drive.serverTick();

    public DiskDriveBlock(Properties settings) {
        super(settings);
        registerDefaultState(getStateDefinition().any()
            .setValue(FACING, Direction.NORTH)
            .setValue(STATE, DiskDriveState.EMPTY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> properties) {
        properties.add(FACING, STATE);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isCrouching() && level.getBlockEntity(pos) instanceof DiskDriveBlockEntity drive) {
            // Try to put a disk into the drive
            if (stack.isEmpty()) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;

            if (!level.isClientSide && drive.getDiskStack().isEmpty() && MediaProviders.get(stack) != null) {
                drive.setDiskStack(stack.split(1));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModRegistry.BlockEntities.DISK_DRIVE.get().create(pos, state);
    }

    @Override
    @Nullable
    public <U extends BlockEntity> BlockEntityTicker<U> getTicker(Level level, BlockState state, BlockEntityType<U> type) {
        return level.isClientSide ? null : BaseEntityBlock.createTickerHelper(type, ModRegistry.BlockEntities.DISK_DRIVE.get(), serverTicker);
    }
}
