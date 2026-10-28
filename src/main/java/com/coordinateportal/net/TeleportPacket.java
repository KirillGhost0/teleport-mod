package com.coordinateportal.net;

import com.coordinateportal.server.TeleportService;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TeleportPacket implements IMessage {
    public static final int COORDS = 0, PLAYER = 1, SPAWN = 2, SAVE = 3, WAYPOINT = 4;
    public int action, dimension;
    public double x, y, z;
    public String target = "";
    public TeleportPacket() {}
    public TeleportPacket(int action, int dimension, double x, double y, double z, String target) {
        this.action=action; this.dimension=dimension; this.x=x; this.y=y; this.z=z; this.target=target == null ? "" : target;
    }
    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(action);buf.writeInt(dimension);buf.writeDouble(x);buf.writeDouble(y);buf.writeDouble(z);
        byte[] bytes;
        try { bytes = target.getBytes("UTF-8"); } catch (java.io.UnsupportedEncodingException ex) { bytes = new byte[0]; }
        int len=Math.min(bytes.length, 80); buf.writeByte(len); buf.writeBytes(bytes, 0, len);
    }
    @Override public void fromBytes(ByteBuf buf) {
        action=buf.readInt();dimension=buf.readInt();x=buf.readDouble();y=buf.readDouble();z=buf.readDouble();
        int len=buf.readUnsignedByte();if(len>80 || len>buf.readableBytes()) throw new IllegalArgumentException("Bad packet length");
        byte[] bytes=new byte[len];buf.readBytes(bytes);
        try { target=new String(bytes,"UTF-8"); } catch(java.io.UnsupportedEncodingException ex) { target=""; }
    }
    public static class Handler implements IMessageHandler<TeleportPacket,IMessage> {
        @Override public IMessage onMessage(TeleportPacket packet, MessageContext ctx) {
            // FML's Netty handlers may run outside the game server thread. Never modify a world here.
            final EntityPlayerMP sender=ctx.getServerHandler().playerEntity;
            TeleportService.enqueue(sender, packet);
            return null;
        }
    }
}
