package com.hrznstudio.spatial;

import com.hrznstudio.spatial.client.GuiConnecting;
import com.hrznstudio.spatial.client.GuiExpanseMenu;
import com.hrznstudio.spatial.client.HorizonClientWorker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = "expanse", name = "Expanse", version = "0.0.1")
@Mod.EventBusSubscriber
public class SpatialMod {

    private static HorizonClientWorker worker = new HorizonClientWorker();

    public static HorizonClientWorker getClientWorker() {
        return worker;
    }

    @SubscribeEvent
    public static void actionEvent(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.getGui() instanceof GuiMainMenu) {
            if (event.getButton().id == 2) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiConnecting());
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void initGuiEvent(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiMainMenu&&!(event.getGui() instanceof GuiExpanseMenu)) {
            event.setGui(new GuiExpanseMenu());
        }
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
    }
}