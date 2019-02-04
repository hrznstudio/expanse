package com.hrznstudio.spatial.client;

import com.hrznstudio.spatial.SpatialMod;
import net.minecraft.client.gui.GuiScreen;

public class GuiConnecting extends GuiScreen { // TODO: add a cancel button to call `SpatialMod.getClientWorker().stop();`

    public GuiConnecting() {
        SpatialMod.getClientWorker().start();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        switch (SpatialMod.getClientWorker().getConnectionStatus()) {
            case CONNECTED:
                drawCenteredString(fontRenderer, "Connected to SpatialOS", width / 2, height / 2, -1);
                return;
            case CONNECTING:
                drawCenteredString(fontRenderer, "Connecting to SpatialOS...", width / 2, height / 2, -1);
                return;
            default:
            case DISCONNECTED:
                drawCenteredString(fontRenderer, "Connection to SpatialOS Failed", width / 2, height / 2, -1);
        }
    }
}