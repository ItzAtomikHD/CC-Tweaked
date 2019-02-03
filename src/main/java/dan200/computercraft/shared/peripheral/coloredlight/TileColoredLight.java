package dan200.computercraft.shared.peripheral.coloredlight;

import dan200.computercraft.shared.peripheral.common.TilePeripheralBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class TileColoredLight extends TilePeripheralBase {

    private static final int[] DEFAULT_RGB = {255,0,0};
    private int[] m_RGB = {255,0,0};

    public int getColor()
    {
        return convertToInt(m_RGB[0], m_RGB[1], m_RGB[2]);
    }

    public void setColor(int r, int g, int b)
    {
        this.m_RGB[0] = r;
        this.m_RGB[1] = g;
        this.m_RGB[2] = b;
    }

    public static int convertToInt(int r, int g, int b)
    {
        return (r * 65536) + (g * 256) + b;
    }


    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt = super.writeToNBT(nbt);
        nbt.setIntArray("rgb", m_RGB);

        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        int[] rgbResult = DEFAULT_RGB;
        if (nbt.hasKey("rgb"))
        {
            rgbResult = nbt.getIntArray("rgb");
        }
        m_RGB = rgbResult;
    }

}
