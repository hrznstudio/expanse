package com.hrznstudio.spatial.mixin;

import com.hrznstudio.spatial.util.ConnectionManager;
import com.hrznstudio.spatial.util.ConnectionStatus;
import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ClientBrandRetriever.class)
public class MixinClientBrandRetriever {

    /**
     * @reason Identify when spatial is running
     * @author Coded
     */
    @Overwrite
    public static String getClientModName() {
        if (ConnectionManager.getConnectionStatus() != ConnectionStatus.DISCONNECTED) {
            return "spatial,SpatialOS," + net.minecraftforge.fml.common.FMLCommonHandler.instance().getModName();
        }
        return net.minecraftforge.fml.common.FMLCommonHandler.instance().getModName();
    }

}
