/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.network.client;

import dan200.computercraft.client.ClientTableFormatter;
import dan200.computercraft.shared.command.text.TableBuilder;
import dan200.computercraft.shared.network.NetworkMessage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.networking.PacketContext;
import net.minecraft.text.TextComponent;
import net.minecraft.util.PacketByteBuf;

import javax.annotation.Nonnull;

public class ChatTableClientMessage implements NetworkMessage
{
    private TableBuilder table;

    public ChatTableClientMessage( TableBuilder table )
    {
        if( table.getColumns() < 0 ) throw new IllegalStateException( "Cannot send an empty table" );
        this.table = table;
    }

    public ChatTableClientMessage()
    {
    }

    @Override
    public void toBytes( @Nonnull PacketByteBuf buf )
    {
        buf.writeVarInt( table.getId() );
        buf.writeVarInt( table.getColumns() );
        buf.writeBoolean( table.getHeaders() != null );
        if( table.getHeaders() != null )
        {
            for( TextComponent header : table.getHeaders() ) buf.writeTextComponent( header );
        }

        buf.writeVarInt( table.getRows().size() );
        for( TextComponent[] row : table.getRows() )
        {
            for( TextComponent column : row ) buf.writeTextComponent( column );
        }
    }

    @Override
    public void fromBytes( @Nonnull PacketByteBuf buf )
    {
        int id = buf.readVarInt();
        int columns = buf.readVarInt();
        TableBuilder table;
        if( buf.readBoolean() )
        {
            TextComponent[] headers = new TextComponent[columns];
            for( int i = 0; i < columns; i++ ) headers[i] = buf.readTextComponent();
            table = new TableBuilder( id, headers );
        }
        else
        {
            table = new TableBuilder( id );
        }

        int rows = buf.readVarInt();
        for( int i = 0; i < rows; i++ )
        {
            TextComponent[] row = new TextComponent[columns];
            for( int j = 0; j < columns; j++ ) row[j] = buf.readTextComponent();
            table.row( row );
        }

        this.table = table;
    }

    @Override
    @Environment( EnvType.CLIENT )
    public void handle( PacketContext context )
    {
        ClientTableFormatter.INSTANCE.display( table );
    }
}
