package com.Radio.RadioMod;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.Radio.RadioMod.blocks.BlockBooster;
import com.Radio.RadioMod.items.ItemRadio;
import com.Radio.RadioMod.items.ItemTelegraphKey;
import com.Radio.RadioMod.network.AudioPacketHandler;

@Mod(modid = RadioMods.MODID, name = RadioMods.NAME, version = RadioMods.VERSION)
public class RadioMods {
    public static final String MODID = "radiomods";
    public static final String NAME = "Radio Mods";
    public static final String VERSION = "1.0";

    public static SimpleNetworkWrapper NETWORK;

    public static ItemRadio radio;
    public static ItemTelegraphKey telegraph_key;
    public static BlockBooster booster1;
    public static BlockBooster booster2;
    public static BlockBooster booster3;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        radio = new ItemRadio();
        telegraph_key = new ItemTelegraphKey();
        booster1 = new BlockBooster(1.5f, 0, "booster1");
        booster2 = new BlockBooster(2.5f, 1, "booster2");
        booster3 = new BlockBooster(4.0f, 2, "booster3");

        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        AudioPacketHandler.register();

        // Клиентская инициализация
        if (event.getSide().isClient()) {
            ClientEventSubscriber.preInit(event);
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Рецепт рации
        GameRegistry.addShapedRecipe(
                new ResourceLocation(MODID, "radio_recipe"),
                new ResourceLocation(MODID, "radio_group"),
                new ItemStack(radio),
                "GSG",
                "RIR",
                " G ",
                'G', Blocks.GLASS_PANE,
                'S', Items.STICK,
                'R', Items.REDSTONE,
                'I', Items.IRON_INGOT
        );

        // Рецепт телеграфного ключа
        GameRegistry.addShapedRecipe(
                new ResourceLocation(MODID, "telegraph_recipe"),
                new ResourceLocation(MODID, "telegraph_group"),
                new ItemStack(telegraph_key),
                " I ",
                " B ",
                " S ",
                'I', Items.IRON_INGOT,
                'B', Blocks.STONE_BUTTON,
                'S', Blocks.STONE
        );

        // Рецепт усилителя 1
        GameRegistry.addShapedRecipe(
                new ResourceLocation(MODID, "booster1_recipe"),
                new ResourceLocation(MODID, "booster_group"),
                new ItemStack(booster1),
                "III",
                "IRI",
                "ICI",
                'I', Blocks.IRON_BLOCK,
                'R', Items.REDSTONE,
                'C', Blocks.CHEST
        );

        // Рецепт усилителя 2
        GameRegistry.addShapedRecipe(
                new ResourceLocation(MODID, "booster2_recipe"),
                new ResourceLocation(MODID, "booster_group"),
                new ItemStack(booster2),
                "GGG",
                "GBG",
                "GGG",
                'G', Blocks.GOLD_BLOCK,
                'B', booster1
        );

        // Рецепт усилителя 3
        GameRegistry.addShapedRecipe(
                new ResourceLocation(MODID, "booster3_recipe"),
                new ResourceLocation(MODID, "booster_group"),
                new ItemStack(booster3),
                "DDD",
                "DBD",
                "DDD",
                'D', Blocks.DIAMOND_BLOCK,
                'B', booster2
        );

        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

        // Клиентская инициализация
        if (event.getSide().isClient()) {
            ClientEventSubscriber.init(event);
        }
    }

    public static class GuiHandler implements net.minecraftforge.fml.common.network.IGuiHandler {
        @Override
        public Object getServerGuiElement(int ID, net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, int x, int y, int z) {
            return null;
        }

        @Override
        public Object getClientGuiElement(int ID, net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, int x, int y, int z) {
            if (ID == 0) {
                ItemStack stack = player.getHeldItemMainhand();
                if (stack.getItem() instanceof ItemRadio) {
                    return new com.Radio.RadioMod.gui.GuiRadio(stack);
                }
            }
            return null;
        }
    }

    @Mod.EventBusSubscriber
    public static class RegistrationHandler {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            event.getRegistry().register(booster1);
            event.getRegistry().register(booster2);
            event.getRegistry().register(booster3);
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            event.getRegistry().register(radio);
            event.getRegistry().register(telegraph_key);
            event.getRegistry().register(new ItemBlock(booster1).setRegistryName(booster1.getRegistryName()));
            event.getRegistry().register(new ItemBlock(booster2).setRegistryName(booster2.getRegistryName()));
            event.getRegistry().register(new ItemBlock(booster3).setRegistryName(booster3.getRegistryName()));
        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void registerModels(ModelRegistryEvent event) {
            registerModel(radio);
            registerModel(telegraph_key);
            registerModel(Item.getItemFromBlock(booster1));
            registerModel(Item.getItemFromBlock(booster2));
            registerModel(Item.getItemFromBlock(booster3));
        }

        @SideOnly(Side.CLIENT)
        private static void registerModel(Item item) {
            ModelResourceLocation model = new ModelResourceLocation(item.getRegistryName(), "inventory");
            net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation(item, 0, model);
        }
    }
}