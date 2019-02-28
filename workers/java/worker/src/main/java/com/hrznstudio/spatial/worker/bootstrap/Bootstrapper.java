package com.hrznstudio.spatial.worker.bootstrap;

import com.hrznstudio.spatial.worker.BaseWorker;
import minecraft.boostrap.Bootstrap;
import minecraft.boostrap.Message;
import minecraft.general.Void;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;

public class Bootstrapper extends BaseWorker.BaseViewWorker {
    public Bootstrapper() {
        net.minecraft.init.Bootstrap.register();
    }

    @Override
    protected void onConnected() {
        getDispatcher().onCommandRequest(Bootstrap.Commands.ON_CHAT, argument -> {
            String message = argument.request.getMessage();
            //TODO: need a player entity
            ServerChatEvent event = new ServerChatEvent(null, message, new TextComponentString(message));
            if (!MinecraftForge.EVENT_BUS.post(event)) {
                Message responseMessage = Message.create();
                responseMessage.setMessage(event.getComponent().getFormattedText());
                getConnection().sendCommandResponse(Bootstrap.Commands.ON_CHAT, argument.requestId, Void.create());
            }
        });
    }
}