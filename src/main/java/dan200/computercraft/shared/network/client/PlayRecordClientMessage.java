/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.network.client;

import dan200.computercraft.shared.network.NetworkMessage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.networking.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nonnull;

/**
 * Starts or stops a record on the client, depending on if {@link #soundEvent} is {@code null}.
 *
 * Used by disk drives to play record items.
 *
 * @see dan200.computercraft.shared.peripheral.diskdrive.TileDiskDrive
 */
public class PlayRecordClientMessage implements NetworkMessage
{
    private final BlockPos pos;
    private final String name;
    private final SoundEvent soundEvent;

    public PlayRecordClientMessage( BlockPos pos, SoundEvent event, String name )
    {
        this.pos = pos;
        this.name = name;
        this.soundEvent = event;
    }

    public PlayRecordClientMessage( BlockPos pos )
    {
        this.pos = pos;
        this.name = null;
        this.soundEvent = null;
    }

    public PlayRecordClientMessage( PacketByteBuf buf )
    {
        pos = buf.readBlockPos();
        if( buf.readBoolean() )
        {
            name = buf.readString( Short.MAX_VALUE );
            soundEvent = Registry.SOUND_EVENT.getInt( buf.readInt() );
        }
        else
        {
            name = null;
            soundEvent = null;
        }
    }

    @Override
    public void toBytes( @Nonnull PacketByteBuf buf )
    {
        buf.writeBlockPos( pos );
        if( soundEvent == null )
        {
            buf.writeBoolean( false );
        }
        else
        {
            buf.writeBoolean( true );
            buf.writeString( name );
            buf.writeInt( Registry.SOUND_EVENT.getRawId( soundEvent ) );
        }
    }

    @Override
    @Environment( EnvType.CLIENT )
    public void handle( PacketContext context )
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.world.playRecord( pos, soundEvent );
        if( name != null ) mc.inGameHud.setRecordPlayingOverlay( name );
    }
}
