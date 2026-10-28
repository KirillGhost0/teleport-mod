package com.coordinateportal.client;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
public class PortalKeyHandler {
    private boolean held = false;
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        boolean combo = Keyboard.isKeyDown(Keyboard.KEY_TAB) && Keyboard.isKeyDown(Keyboard.KEY_Q);
        Minecraft mc = Minecraft.getMinecraft();
        if (combo && !held && mc.thePlayer != null && mc.currentScreen == null) mc.displayGuiScreen(new PortalGui());
        held = combo;
    }
}
