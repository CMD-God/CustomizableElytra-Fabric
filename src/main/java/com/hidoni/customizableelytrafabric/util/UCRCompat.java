package com.hidoni.customizableelytrafabric.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class UCRCompat {

    private static final String ELYTRA_POWER_KEY = new Identifier("ucr_stitch", "elytra_power").toString();
    
    public static boolean copyUpgradeInfo(ItemStack from, ItemStack to) {
        NbtCompound fromNbt = from.getOrCreateNbt();
        if (!fromNbt.contains(ELYTRA_POWER_KEY, NbtCompound.STRING_TYPE)) {
            return false;
        }
        to.getOrCreateNbt().putString(ELYTRA_POWER_KEY, fromNbt.getString(ELYTRA_POWER_KEY));
        return true;
    }

}
