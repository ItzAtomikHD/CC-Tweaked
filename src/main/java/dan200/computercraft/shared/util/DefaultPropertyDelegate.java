/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.util;

import net.minecraft.container.PropertyDelegate;

public interface DefaultPropertyDelegate extends PropertyDelegate
{
    @Override
    default void set( int property, int value )
    {
    }
}
