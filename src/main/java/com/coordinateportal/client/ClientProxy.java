package com.coordinateportal.client;
import com.coordinateportal.CommonProxy;
import cpw.mods.fml.common.FMLCommonHandler;
public class ClientProxy extends CommonProxy {
    @Override public void init() { FMLCommonHandler.instance().bus().register(new PortalKeyHandler()); }
}
