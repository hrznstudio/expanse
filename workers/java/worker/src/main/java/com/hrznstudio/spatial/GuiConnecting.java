package com.hrznstudio.spatial;

import net.minecraft.client.gui.GuiScreen;

public class GuiConnecting extends GuiScreen {
    public GuiConnecting() {
        ConnectionManager.connect();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if(ConnectionManager.getConnectionStatus().isConnected()) {
            SpatialMod.getClientWorker().initializeConnection();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        switch (ConnectionManager.getConnectionStatus()) {
            case CONNECTED:
                drawCenteredString(fontRenderer, "Connected to SpatialOS", width / 2, height / 2, -1);
                return;
            case CONNECTING:
                drawCenteredString(fontRenderer, "Connecting to SpatialOS...", width / 2, height / 2, -1);
                return;
            default:
            case DISCONNECTED:
                drawCenteredString(fontRenderer, "Connection to SpatialOS Failed", width / 2, height / 2, -1);
                return;
        }
    }
}