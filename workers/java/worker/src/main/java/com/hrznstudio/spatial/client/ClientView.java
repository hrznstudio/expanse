package com.hrznstudio.spatial.client;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.hrznstudio.spatial.SpatialMod;
import com.hrznstudio.spatial.api.ISpatialWorld;
import com.hrznstudio.spatial.client.vanillawrappers.WorldClientSpatial;
import com.hrznstudio.spatial.util.Converters;
import com.hrznstudio.spatial.util.EntityRequirementCallback;
import com.hrznstudio.spatial.util.Util;
import com.hrznstudio.spatial.worker.chunk.ChunkWorker;
import com.mojang.authlib.GameProfile;
import improbable.Coordinates;
import improbable.Position;
import improbable.worker.Entity;
import improbable.worker.EntityId;
import improbable.worker.View;
import minecraft.entity.PlayerInfo;
import minecraft.entity.PlayerInfoData;
import minecraft.entity.Rotation;
import minecraft.entity.RotationData;
import minecraft.world.ChunkStorage;
import minecraft.world.ChunkStorageData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.EntityTracker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientView extends View {

    private final ChunkStorageData empty = ChunkStorageData.create();
    private final BiMap<BlockPos, EntityId> posToIdChunks = HashBiMap.create();
    private final BiMap<EntityId, BlockPos> idToPosChunks = posToIdChunks.inverse();
    public final List<EntityId> players = new ArrayList<>();

    public ClientView() {
        this.onRemoveEntity(op -> removeEntity(op.entityId));
        EntityRequirementCallback.builder(
                (id) -> {
                    Entity entity = getEntity(id);
                    BlockPos pos = Converters.improbableToChunkPos(Util.getData(entity, Position.COMPONENT));
                    addChunk(pos, id);
                    ((WorldClientSpatial) Minecraft.getMinecraft().world).loadChunk(pos, Util.getData(entity, ChunkStorage.COMPONENT));
                })
                .requireType(ChunkWorker.CHUNK)
                .requireComponent(Position.COMPONENT)
                .requireComponent(ChunkStorage.COMPONENT)
                .attach(this);
        EntityRequirementCallback.builder(
                (id) -> {
                    if(id== SpatialMod.getClientWorker().getPlayerId())
                        return;
                    WorldClient world = Minecraft.getMinecraft().world;
                    if (world == null) {
                        players.add(id);
                    } else {
                        Entity entity = getEntity(id);
                        Coordinates pos = Util.getData(entity, Position.COMPONENT).getCoords();
                        PlayerInfoData player = Util.getData(entity, PlayerInfo.COMPONENT);
                        RotationData rotation = Util.getData(entity, Rotation.COMPONENT);
                        GameProfile profile = new GameProfile(UUID.fromString(player.getProfile().getUuid()), player.getProfile().getName());
                        EntityOtherPlayerMP mp = new EntityOtherPlayerMP(world, profile);
                        double d0 = pos.getX();
                        double d1 = pos.getY();
                        double d2 = pos.getZ();
                        float f = (rotation.getYaw() * 360) / 256.0F;
                        float f1 = (rotation.getPitch() * 360) / 256.0F;
                        mp.prevPosX = d0;
                        mp.lastTickPosX = d0;
                        mp.prevPosY = d1;
                        mp.lastTickPosY = d1;
                        mp.prevPosZ = d2;
                        mp.lastTickPosZ = d2;
                        EntityTracker.updateServerPosition(mp, d0, d1, d2);
                        mp.setPositionAndRotation(d0, d1, d2, f, f1);
                        world.addEntityToWorld(mp.getEntityId(), mp);
                    }
                })
                .requireType("Player")
                .requireComponent(PlayerInfo.COMPONENT)
                .requireComponent(Position.COMPONENT)
                .attach(this);
        onComponentUpdate(Position.COMPONENT, argument -> {
            if (Util.getType(entities.get(argument.entityId)).equals("Player")) {
                Coordinates coords = argument.update.getCoords().orElse(null);
                if (coords != null) {
                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        WorldClient world = Minecraft.getMinecraft().world;
                        if (argument.entityId.equals(SpatialMod.getClientWorker().getPlayerId())) {
                            double dist = 0.5;
                            boolean x = Util.isWithin(Minecraft.getMinecraft().player.posX, coords.getX(), dist);
                            boolean y = Util.isWithin(Minecraft.getMinecraft().player.posY, coords.getY(), dist);
                            boolean z = Util.isWithin(Minecraft.getMinecraft().player.posZ, coords.getZ(), dist);
                            if (!x && !y && !z) {
                                ((ISpatialWorld) world).getEntityById(argument.entityId).setPositionAndUpdate(coords.getX(), coords.getY(), coords.getZ());
                            }
                        } else {
                            ((ISpatialWorld) world).getEntityById(argument.entityId).setPositionAndUpdate(coords.getX(), coords.getY(), coords.getZ());
                        }
                    });
                }
            }
        });
    }

    @Nullable
    public Entity getEntity(EntityId id) {
        return entities.get(id);
    }

    private void removeEntity(EntityId id) {
        String type = Util.getType(getEntity(id));
        if (type.equals(ChunkWorker.CHUNK))
            removeChunk(id);
        else if (type.equals("Player")) {
            removePlayer(id);
        }
    }

    private void removePlayer(EntityId player) {
        players.remove(player);
        if (Minecraft.getMinecraft().world != null) {
            Minecraft.getMinecraft().world.removeEntity(((ISpatialWorld) Minecraft.getMinecraft().world).getEntityById(player));
        }
    }

    private void removeChunk(EntityId chunk) {
        BlockPos pos = idToPosChunks.remove(chunk);
        if (pos != null) ((WorldClientSpatial) Minecraft.getMinecraft().world).unloadChunk(pos);
    }

    private void addChunk(BlockPos pos, EntityId chunk) {
        posToIdChunks.put(pos, chunk);
    }

    /**
     * @param pos chunk position, in chunk coordinates
     * @return chunk at given position
     */
    public ChunkStorageData getChunk(final BlockPos pos) {
        EntityId id = posToIdChunks.get(pos);
        if (id == null) return empty;
        Entity entity = getEntity(id);
        if (entity == null) return empty;
        return entity.get(ChunkStorage.COMPONENT).orElse(empty);
    }

    /**
     * @param pos chunk position, in block coordinates
     * @return chunk at given position
     */
    public ChunkStorageData getChunkFromBlock(final Vec3i pos) {
        return getChunkFromBlock(pos.getX(), pos.getY(), pos.getZ());
    }

    public ChunkStorageData getChunkFromBlock(final int x, final int y, final int z) {
        return getChunk(new BlockPos(x >> 4, y >> 4, z >> 4));
    }
}
