package com.coordinateportal;

import com.coordinateportal.net.TeleportPacket;
import com.coordinateportal.server.TeleportService;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.common.SidedProxy;

@Mod(modid = CoordinatePortal.MODID, name = "Coordinate Portal", version = "1.0.0", acceptableRemoteVersions = "*")
public class CoordinatePortal {
    public static final String MODID = "coordinateportal";
    public static SimpleNetworkWrapper NETWORK;
    @SidedProxy(clientSide="com.coordinateportal.client.ClientProxy", serverSide="com.coordinateportal.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler public void preInit(FMLPreInitializationEvent event) {
        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        NETWORK.registerMessage(TeleportPacket.Handler.class, TeleportPacket.class, 0, Side.SERVER);
    }
    @Mod.EventHandler public void init(FMLInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(new TeleportService());
        proxy.init();
    }
}
