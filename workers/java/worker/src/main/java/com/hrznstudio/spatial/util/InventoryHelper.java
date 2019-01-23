package com.hrznstudio.spatial.util;

import improbable.collections.Option;
import improbable.worker.Bytes;
import minecraft.item.Item;
import minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;

public class InventoryHelper {

    public static final ItemStack EMPTY = fromStack(net.minecraft.item.ItemStack.EMPTY);

    public static ItemStack fromStack(net.minecraft.item.ItemStack stack) {
        ItemStack itemStack = ItemStack.create();
        itemStack.setItem(fromItem(stack.getItem()));
        itemStack.setCount(stack.getCount());
        itemStack.setDamage(stack.getItemDamage());
        if (stack.hasTagCompound()) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                CompressedStreamTools.writeCompressed(stack.getTagCompound(), byteArrayOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            itemStack.setNbt(Option.of(Bytes.copyOf(byteArrayOutputStream.toByteArray())));
        } else {
            itemStack.setNbt(Option.empty());
        }
        return itemStack;
    }

    public static Item fromItem(net.minecraft.item.Item item) {
        Item spatialItem = Item.create();
        spatialItem.setId(item.getRegistryName().toString());
        return spatialItem;
    }
}