package com.hrznstudio.spatial;

import com.hrznstudio.spatial.client.ClientWorker;
import com.hrznstudio.spatial.client.GuiConnecting;
import com.hrznstudio.spatial.util.CommonWorkerRequirements;
import com.hrznstudio.spatial.util.EntityBuilder;
import improbable.Coordinates;
import improbable.Position;
import improbable.PositionData;
import improbable.worker.Entity;
import minecraft.entity.*;
import minecraft.inventory.Inventory;
import minecraft.inventory.InventoryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.UUID;

@Mod(modid = "spatial", name = "Spatial", version = "0.0.1")
@Mod.EventBusSubscriber
public class SpatialMod {

    private static ClientWorker worker = new ClientWorker();

    public static ClientWorker getClientWorker() {
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

    public static Entity createPlayerEntity() {
        EntityBuilder builder = new EntityBuilder("Player");
        builder.addComponent(
                Position.COMPONENT,
                new PositionData(new Coordinates(0, 0, 0)),
                CommonWorkerRequirements.getEntityWorkers()
        );
        builder.addComponent(
                Player.COMPONENT,
                new PlayerData(new GameProfile(UUID.randomUUID().toString()), false, false),
                CommonWorkerRequirements.getEntityWorkers()
        );
        builder.addComponent(
                Health.COMPONENT,
                new HealthData(20, 20),
                CommonWorkerRequirements.getEntityWorkers()
        );
        builder.addComponent(
                Food.COMPONENT,
                new FoodData(20, 20),
                CommonWorkerRequirements.getEntityWorkers()
        );
        builder.addComponent(
                Experience.COMPONENT,
                new ExperienceData(0),
                CommonWorkerRequirements.getEntityWorkers()
        );
        builder.addComponent(
                Inventory.COMPONENT,
                new InventoryData(Collections.emptyMap()),
                CommonWorkerRequirements.getEntityWorkers()
        );
        builder.addComponent(
                Flammable.COMPONENT,
                new FlammableData(false),
                CommonWorkerRequirements.getEntityWorkers()
        );
        return builder.build();
    }
}
