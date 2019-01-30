/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.speaker;

import dan200.computercraft.shared.common.BlockGeneric;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

public class BlockSpeaker extends BlockGeneric
{
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BlockSpeaker( Builder settings )
    {
        super( settings, TileSpeaker.FACTORY );
        setDefaultState( getStateContainer().getBaseState()
            .with( FACING, EnumFacing.NORTH ) );
    }

    @Override
    protected void fillStateContainer( StateContainer.Builder<Block, IBlockState> properties )
    {
        properties.add( FACING );
    }

    @Nullable
    @Override
    public IBlockState getStateForPlacement( BlockItemUseContext placement )
    {
        return getDefaultState().with( FACING, placement.getPlacementHorizontalFacing().getOpposite() );
    }
}
