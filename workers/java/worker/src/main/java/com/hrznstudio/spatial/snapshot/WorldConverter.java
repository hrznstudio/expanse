package com.hrznstudio.spatial.snapshot;

import com.google.gson.Gson;
import com.hrznstudio.spatial.data.BlockData;
import com.hrznstudio.spatial.util.Converters;
import com.hrznstudio.spatial.util.EntityBuilder;
import improbable.Coordinates;
import improbable.Position;
import improbable.WorkerAttributeSet;
import improbable.WorkerRequirementSet;
import improbable.worker.EntityId;
import improbable.worker.SnapshotOutputStream;
import minecraft.world.Block;
import minecraft.world.ChunkStorage;
import minecraft.world.ChunkStorageData;
import minecraft.world.State;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraftforge.common.util.Constants;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class WorldConverter {
    private static int worldSize = 64;
    private static final String CHUNK = "chunk";
    private static final WorkerRequirementSet CHUNK_REQUIREMENT_SET = new WorkerRequirementSet(Collections.singletonList(new WorkerAttributeSet(Collections.singletonList("chunk_worker"))));

    public static void main(String[] args) throws Exception {
        SnapshotOutputStream outputStream = new SnapshotOutputStream("default.snapshot");
        final int chunks = (worldSize/2) >> 4;
        final int negativeChunks = -chunks;
        long currentEntityId = 1;
        File world = new File("world");
        NBTTagCompound leveldat = CompressedStreamTools.readCompressed(new FileInputStream(new File(world, "level.dat")));
        Map<Integer, Block> map = new HashMap<>();
        if(leveldat.hasKey("FML")) {
            leveldat
                    .getCompoundTag("FML")
                    .getCompoundTag("Registries")
                    .getCompoundTag("minecraft:blocks")
                    .getTagList("ids", 10)
                    .forEach(nbtBase -> map.put((int)((NBTTagCompound) nbtBase).getByte("V"), new Block(((NBTTagCompound) nbtBase).getString("K"))));
        } else {
            Gson gson = new Gson();
            BlockData[] arr = gson.fromJson(new FileReader(new File("data.json")), BlockData[].class);
            Stream.of(arr).forEach(blockData -> map.putIfAbsent(blockData.type, new Block("minecraft:" + blockData.textType)));
        }
        for (int x = negativeChunks; x < chunks; x++) {
            for (int z = negativeChunks; z < chunks; z++) {
                System.out.printf("Loading chunk x%d z%d%n", x, z);
                DataInputStream stream = RegionFileCache.getChunkInputStream(world, x, z);
                if (stream == null) {
                    System.out.println("Chunk did not exist, skipping");
                } else {
                    try {
                        NBTTagCompound compound = CompressedStreamTools.read(stream);
                        CompressedStreamTools.write(compound, new File("export/" + x + "x" + z + ".dat"));
                        Map<Integer, Map<Integer, State>> chunkData = new HashMap<>();
                        NBTTagCompound level = compound.getCompoundTag("Level");
                        NBTTagList sections = level.getTagList("Sections", 10);
                        for(int wah = 0; wah <sections.tagCount(); wah++) {
                            NBTTagCompound section = sections.getCompoundTagAt(wah);
                            byte[] metaArray = section.getByteArray("Data");
                            byte[] addArray = null;
                            if(section.hasKey("Add", 7))
                                addArray=section.getByteArray("Add");
                            NibbleArray extensionNibble = null;
                            if(addArray==null)
                                extensionNibble=new NibbleArray(addArray);
                            NibbleArray metaNibble = new NibbleArray(metaArray);
                            byte[] byteArray = section.getByteArray("Blocks");
                            for (int i = 0; i < byteArray.length; i++) {
                                byte b = byteArray[i];
                                if (map.get(b).getId().equals("minecraft:air"))
                                    continue;
                                int j = i & 15; //x
                                int k = (i >> 8) & 15; //y
                                int l = i >> 4 & 15; //z
                                chunkData.putIfAbsent((int) section.getByte("Y"), new HashMap<>());
                                int extensionID = extensionNibble == null ? 0 : extensionNibble.get(j,k,l);
                                int blockID = extensionID << 8 | (b & 255);
                                int meta = metaNibble.get(j,k,l);
                                chunkData.get((int) section.getByte("Y")).put(Converters.blockPosToChunkIndex(j, k, l), new State(map.get(blockID), meta));
                            }
                        }

                        for (Map.Entry<Integer, Map<Integer, State>> data : chunkData.entrySet()) {
                            if (!data.getValue().isEmpty()) {
                                EntityBuilder builder = new EntityBuilder(CHUNK);
                                builder.addComponent(ChunkStorage.COMPONENT, new ChunkStorageData(data.getValue()), CHUNK_REQUIREMENT_SET);
                                builder.addComponent(Position.COMPONENT, new improbable.PositionData(new Coordinates(x * 16, data.getKey() * 16, z * 16)), CHUNK_REQUIREMENT_SET);
                                outputStream.writeEntity(new EntityId(currentEntityId++), builder.build());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        outputStream.close();
    }
}