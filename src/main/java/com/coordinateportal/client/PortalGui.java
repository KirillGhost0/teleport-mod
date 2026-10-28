package com.coordinateportal.client;

import com.coordinateportal.CoordinatePortal;
import com.coordinateportal.net.TeleportPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import java.util.ArrayList;
import java.util.List;

public class PortalGui extends GuiScreen {
    private GuiTextField fieldX, fieldY, fieldZ, fieldPlayer;
    private int left, top;
    private String status="";
    private final List<GuiTextField> fields=new ArrayList<GuiTextField>();

    @Override public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear(); fields.clear();
        left=width/2-125; top=height/2-113;
        fieldX=addField(0,left+12,top+51,70,18,String.format(java.util.Locale.US,"%.1f",mc.thePlayer.posX));
        fieldY=addField(1,left+90,top+51,70,18,String.format(java.util.Locale.US,"%.1f",mc.thePlayer.posY));
        fieldZ=addField(2,left+168,top+51,70,18,String.format(java.util.Locale.US,"%.1f",mc.thePlayer.posZ));
        fieldPlayer=addField(3,left+12,top+117,226,18,"");
        buttonList.add(new GuiButton(1,left+12,top+74,226,20,"Teleport to coordinates"));
        buttonList.add(new GuiButton(2,left+12,top+141,226,20,"Teleport to player"));
        buttonList.add(new GuiButton(3,left+12,top+167,108,20,"World spawn"));
        buttonList.add(new GuiButton(4,left+130,top+167,108,20,"Saved waypoint"));
        buttonList.add(new GuiButton(5,left+12,top+191,226,20,"Save current location as waypoint"));
    }
    private GuiTextField addField(int id,int x,int y,int w,int h,String initial) {
        GuiTextField field=new GuiTextField(fontRendererObj,id,x,y,w,h);
        field.setMaxStringLength(64);field.setText(initial);fields.add(field);return field;
    }
    @Override protected void actionPerformed(GuiButton button) {
        try {
            switch(button.id) {
                case 1:
                    double x=Double.parseDouble(fieldX.getText().trim().replace(',','.'));
                    double y=Double.parseDouble(fieldY.getText().trim().replace(',','.'));
                    double z=Double.parseDouble(fieldZ.getText().trim().replace(',','.'));
                    send(TeleportPacket.COORDS,x,y,z,"");break;
                case 2:
                    String name=fieldPlayer.getText().trim();
                    if(name.length()==0) {status="Enter a player name.";return;}
                    send(TeleportPacket.PLAYER,0,0,0,name);break;
                case 3: send(TeleportPacket.SPAWN,0,0,0,"");break;
                case 4: send(TeleportPacket.WAYPOINT,0,0,0,"");break;
                case 5: send(TeleportPacket.SAVE,0,0,0,"");break;
            }
            mc.displayGuiScreen(null);
        } catch(NumberFormatException ex) { status="Invalid coordinates. Example: 120, 64, -30"; }
    }
    private void send(int action,double x,double y,double z,String target) {
        CoordinatePortal.NETWORK.sendToServer(new TeleportPacket(action, mc.thePlayer.dimension,x,y,z,target));
    }
    @Override public void drawScreen(int mouseX,int mouseY,float delta) {
        drawDefaultBackground();
        drawRect(left,top,left+250,top+221,0xE5101721);
        drawRect(left,top,left+250,top+3,0xFF42C9B1);
        drawCenteredString(fontRendererObj,"COORDINATE PORTAL",width/2,top+11,0xFF80EEDC);
        fontRendererObj.drawString("Coordinates (current dimension)",left+12,top+37,0xFFE1E9EF);
        fontRendererObj.drawString("Player nickname (online)",left+12,top+103,0xFFE1E9EF);
        fontRendererObj.drawString("X",left+12,top+43,0xFF8BB5C2);
        fontRendererObj.drawString("Y",left+90,top+43,0xFF8BB5C2);
        fontRendererObj.drawString("Z",left+168,top+43,0xFF8BB5C2);
        for(GuiTextField f:fields) f.drawTextBox();
        if(!status.isEmpty()) drawCenteredString(fontRendererObj,status,width/2,top+226,0xFFFF7777);
        super.drawScreen(mouseX,mouseY,delta);
    }
    @Override protected void mouseClicked(int x,int y,int button) {
        super.mouseClicked(x,y,button);
        for(GuiTextField field:fields) field.mouseClicked(x,y,button);
    }
    @Override protected void keyTyped(char c,int code) {
        if(code==Keyboard.KEY_ESCAPE) {mc.displayGuiScreen(null);return;}
        for(GuiTextField field:fields) if(field.isFocused()) {field.textboxKeyTyped(c,code);return;}
        super.keyTyped(c,code);
    }
    @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
    @Override public boolean doesGuiPauseGame() {return false;}
}
