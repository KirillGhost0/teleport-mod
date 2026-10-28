package com.coordinateportal.server;

import com.coordinateportal.net.TeleportPacket;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldServer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TeleportService {
    private static class Request {
        final EntityPlayerMP player; final TeleportPacket packet;
        Request(EntityPlayerMP p, TeleportPacket m) { player=p; packet=m; }
    }
    private static final Queue<Request> QUEUE=new ConcurrentLinkedQueue<Request>();
    public static void enqueue(EntityPlayerMP p, TeleportPacket m) { QUEUE.add(new Request(p,m)); }
    @SubscribeEvent public void onServerTick(TickEvent.ServerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END) return;
        Request req;
        int processed=0;
        while(processed++<200 && (req=QUEUE.poll())!=null) {
            try { if(req.player.playerNetServerHandler!=null) handle(req.player,req.packet); }
            catch(Exception ex) { message(req.player,"Teleport failed: " + ex.getClass().getSimpleName()); }
        }
    }
    private static void handle(EntityPlayerMP p, TeleportPacket m) {
        MinecraftServer server=MinecraftServer.getServer();
        if(server==null) return;
        // Optional permission restrictions are intended to be applied here by server owners.
        if(m.action==TeleportPacket.SAVE) {
            NBTTagCompound root=p.getEntityData();
            NBTTagCompound persisted=root.hasKey(EntityPlayerMP.PERSISTED_NBT_TAG,10) ? root.getCompoundTag(EntityPlayerMP.PERSISTED_NBT_TAG) : new NBTTagCompound();
            NBTTagCompound point=new NBTTagCompound();
            point.setInteger("Dimension",p.dimension);point.setDouble("X",p.posX);point.setDouble("Y",p.posY);point.setDouble("Z",p.posZ);
            persisted.setTag("CoordinatePortalWaypoint",point);
            root.setTag(EntityPlayerMP.PERSISTED_NBT_TAG,persisted);
            message(p,"Waypoint saved at your current location.");return;
        }
        int dim=p.dimension;double x=0,y=0,z=0;
        if(m.action==TeleportPacket.COORDS) {
            if(!finite(m.x)||!finite(m.y)||!finite(m.z)||Math.abs(m.x)>29999984||Math.abs(m.z)>29999984||m.y<1||m.y>254) {message(p,"Coordinates must be within world limits (Y: 1..254).");return;}
            dim=p.dimension;x=m.x;y=m.y;z=m.z;
        } else if(m.action==TeleportPacket.PLAYER) {
            EntityPlayerMP other=server.getConfigurationManager().func_152612_a(m.target.trim());
            if(other==null) {message(p,"Player not found or offline.");return;}
            dim=other.dimension;x=other.posX;y=other.posY;z=other.posZ;
        } else if(m.action==TeleportPacket.SPAWN) {
            WorldServer overworld=server.worldServerForDimension(0);
            if(overworld==null) {message(p,"Overworld unavailable.");return;}
            ChunkCoordinates spawn=overworld.getSpawnPoint();dim=0;
            x=spawn.posX+0.5;y=spawn.posY+1;z=spawn.posZ+0.5;
        } else if(m.action==TeleportPacket.WAYPOINT) {
            NBTTagCompound root=p.getEntityData();
            NBTTagCompound persisted=root.getCompoundTag(EntityPlayerMP.PERSISTED_NBT_TAG);
            if(!persisted.hasKey("CoordinatePortalWaypoint",10)) {message(p,"No waypoint saved. Save one first.");return;}
            NBTTagCompound point=persisted.getCompoundTag("CoordinatePortalWaypoint");
            dim=point.getInteger("Dimension");x=point.getDouble("X");y=point.getDouble("Y");z=point.getDouble("Z");
        } else return;
        if(server.worldServerForDimension(dim)==null) {message(p,"Destination dimension unavailable.");return;}
        if(!finite(x)||!finite(y)||!finite(z)) return;
        p.mountEntity(null);
        if(p.dimension!=dim) server.getConfigurationManager().transferPlayerToDimension(p,dim);
        p.setPositionAndUpdate(x,y,z);
        p.fallDistance=0;
        message(p,"Teleported successfully.");
    }
    private static boolean finite(double d) {return !Double.isNaN(d)&&!Double.isInfinite(d);}
    private static void message(EntityPlayerMP player,String msg) {player.addChatMessage(new ChatComponentText("[Portal] "+msg));}
}
