package com.hidoni.customizableelytrafabric.item;

import com.hidoni.customizableelytrafabric.CustomizableElytra;
import com.hidoni.customizableelytrafabric.util.ElytraCustomizationUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.entity.event.v1.FabricElytraItem;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CustomizableElytraItem extends ElytraItem implements DyeableItem, FabricElytraItem {
    public final static String LEFT_WING_TRANSLATION_KEY = "item.customizableelytra.left_wing";
    public final static String RIGHT_WING_TRANSLATION_KEY = "item.customizableelytra.right_wing";
    public final static String HIDDEN_CAPE_TRANSLATION_KEY = "item.customizableelytra.cape_hidden";
    public final static String GLOWING_WING_TRANSLATION_KEY = "item.customizableelytra.glowing_wing";

    public CustomizableElytraItem(Settings settings) {
        super(settings);
    }

    @Environment(EnvType.CLIENT)
    public static Identifier getTextureLocation(BannerPattern bannerIn) {
        return new Identifier(CustomizableElytra.MOD_ID, "entity/elytra_banner/" + bannerIn.getName());
    }

    @Override
    public int getColor(ItemStack stack) {
        return getColor(stack, 0);
    }

    public int getColor(ItemStack stack, int index) {
        return ElytraCustomizationUtil.getData(stack).handler.getColor(index);
    }

    @Override
    public boolean hasColor(ItemStack stack) {
        NbtCompound bannerTag = stack.getSubNbt("BlockEntityTag");
        NbtCompound wingTag = stack.getSubNbt("WingInfo");
        return DyeableItem.super.hasColor(stack) || bannerTag != null || wingTag != null || stack.getOrCreateNbt().getInt("WingLightLevel") > 0 || stack.getOrCreateNbt().getBoolean("HideCapePattern");
    }

    @Override
    public void removeColor(ItemStack stack) {
        DyeableItem.super.removeColor(stack);
        NbtCompound compoundTag = stack.getSubNbt("BlockEntityTag");
        if (compoundTag != null) {
            stack.removeSubNbt("BlockEntityTag");
        }
        compoundTag = stack.getSubNbt("WingInfo");
        if (compoundTag != null) {
            stack.removeSubNbt("WingInfo");
        }
        stack.getOrCreateNbt().remove("HideCapePattern");
        stack.getOrCreateNbt().remove("WingLightLevel");
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        applyTooltip(tooltip, context, stack.getNbt(), true);
        NbtCompound wingInfo = stack.getSubNbt("WingInfo");
        if (wingInfo != null) {
            if (wingInfo.contains("left")) {
                NbtCompound leftWing = wingInfo.getCompound("left");
                if (!leftWing.isEmpty()) {
                    tooltip.add(new TranslatableText(LEFT_WING_TRANSLATION_KEY).formatted(Formatting.GRAY));
                    applyTooltip(tooltip, context, leftWing);
                }
            }
            if (wingInfo.contains("right")) {
                NbtCompound rightWing = wingInfo.getCompound("right");
                if (!rightWing.isEmpty()) {
                    tooltip.add(new TranslatableText(RIGHT_WING_TRANSLATION_KEY).formatted(Formatting.GRAY));
                    applyTooltip(tooltip, context, rightWing);
                }
            }
        }
    }

    @Override
    public String getTranslationKey() {
        return Items.ELYTRA.getTranslationKey();
    }

    public static void applyTooltip(List<Text> tooltip, TooltipContext context, NbtCompound wingIn) {
        applyTooltip(tooltip, context, wingIn, false);
    }

    public static void applyTooltip(List<Text> tooltip, TooltipContext context, NbtCompound wingIn, boolean ignoreDisplayTag) {
        NbtCompound wing = ElytraCustomizationUtil.migrateOldSplitWingFormat(wingIn);
        if (wing.getBoolean("HideCapePattern")) {
            tooltip.add(new TranslatableText(HIDDEN_CAPE_TRANSLATION_KEY).formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (wing.getInt("WingLightLevel") > 0) {
            tooltip.add(new TranslatableText(GLOWING_WING_TRANSLATION_KEY).formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (!ignoreDisplayTag && wing.contains("display")) {
            NbtCompound displayTag = wing.getCompound("display");
            if (context.isAdvanced()) {
                tooltip.add((new TranslatableText("item.color", String.format("#%06X", displayTag.getInt("color")))).formatted(Formatting.GRAY));
            } else {
                tooltip.add((new TranslatableText("item.dyed")).formatted(Formatting.GRAY, Formatting.ITALIC));
            }
        } else if (wing.contains("BlockEntityTag")) {
            NbtCompound blockEntityTag = wing.getCompound("BlockEntityTag");
            int baseColor = blockEntityTag.getInt("Base");
            tooltip.add((new TranslatableText("block.minecraft.banner." + BannerPattern.BASE.getName() + '.' + DyeColor.byId(baseColor).getName())).formatted(Formatting.GRAY));
            NbtList listnbt = blockEntityTag.getList("Patterns", 10);

            for (int i = 0; i < listnbt.size() && i < 6; ++i) {
                NbtCompound patternNBT = listnbt.getCompound(i);
                DyeColor dyecolor = DyeColor.byId(patternNBT.getInt("Color"));
                BannerPattern bannerpattern = BannerPattern.byId(patternNBT.getString("Pattern"));
                if (bannerpattern != null) {
                    tooltip.add((new TranslatableText("block.minecraft.banner." + bannerpattern.getName() + '.' + dyecolor.getName())).formatted(Formatting.GRAY));
                }
            }
        }
    }
}
