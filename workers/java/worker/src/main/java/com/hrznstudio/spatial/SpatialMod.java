package com.hrznstudio.spatial;

import com.hrznstudio.spatial.client.GuiConnecting;
import com.hrznstudio.spatial.client.HorizonClientWorker;
import com.hrznstudio.spatial.util.Util;
import com.mojang.authlib.GameProfile;
import improbable.Coordinates;
import improbable.Position;
import improbable.worker.Entity;
import minecraft.entity.PlayerInfo;
import minecraft.entity.PlayerInfoData;
import minecraft.entity.Rotation;
import minecraft.entity.RotationData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.EntityTracker;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.UUID;

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

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
    }
}
