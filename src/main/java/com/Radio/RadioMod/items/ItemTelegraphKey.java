package com.Radio.RadioMod.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import com.Radio.RadioMod.network.AudioPacketHandler;

public class ItemTelegraphKey extends Item {
    public static KeyBinding keyMorse;
    private static boolean[] morseBuffer = new boolean[100];
    private static int bufferIndex = 0;
    private static long lastClick = 0;

    public ItemTelegraphKey() {
        setRegistryName("telegraph_key");
        setUnlocalizedName("telegraph_key");
        setCreativeTab(net.minecraft.creativetab.CreativeTabs.TOOLS);
        setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    public static void initKey() {
        keyMorse = new KeyBinding("key.telegraph.morse", Keyboard.KEY_T, "key.categories.radio");
        ClientRegistry.registerKeyBinding(keyMorse);
    }

    private static String decodeMorse() {
        StringBuilder morse = new StringBuilder();
        for (int i = 0; i < bufferIndex; i++) {
            morse.append(morseBuffer[i] ? "-" : ".");
        }
        String morseStr = morse.toString();

        if (morseStr.equals(".-")) return "A";
        if (morseStr.equals("-...")) return "B";
        if (morseStr.equals("-.-.")) return "C";
        if (morseStr.equals("-..")) return "D";
        if (morseStr.equals(".")) return "E";
        if (morseStr.equals("..-.")) return "F";
        if (morseStr.equals("--.")) return "G";
        if (morseStr.equals("....")) return "H";
        if (morseStr.equals("..")) return "I";
        if (morseStr.equals(".---")) return "J";
        if (morseStr.equals("-.-")) return "K";
        if (morseStr.equals(".-..")) return "L";
        if (morseStr.equals("--")) return "M";
        if (morseStr.equals("-.")) return "N";
        if (morseStr.equals("---")) return "O";
        if (morseStr.equals(".--.")) return "P";
        if (morseStr.equals("--.-")) return "Q";
        if (morseStr.equals(".-.")) return "R";
        if (morseStr.equals("...")) return "S";
        if (morseStr.equals("-")) return "T";
        if (morseStr.equals("..-")) return "U";
        if (morseStr.equals("...-")) return "V";
        if (morseStr.equals(".--")) return "W";
        if (morseStr.equals("-..-")) return "X";
        if (morseStr.equals("-.--")) return "Y";
        if (morseStr.equals("--..")) return "Z";
        if (morseStr.equals("-----")) return "0";
        if (morseStr.equals(".----")) return "1";
        if (morseStr.equals("..---")) return "2";
        if (morseStr.equals("...--")) return "3";
        if (morseStr.equals("....-")) return "4";
        if (morseStr.equals(".....")) return "5";
        if (morseStr.equals("-....")) return "6";
        if (morseStr.equals("--...")) return "7";
        if (morseStr.equals("---..")) return "8";
        if (morseStr.equals("----.")) return "9";
        return "";
    }

    @SideOnly(Side.CLIENT)
    public static class ClientTick {
        private static boolean wasPressed = false;

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            if (Minecraft.getMinecraft().player == null) return;
            if (keyMorse == null) return;

            ItemStack stack = Minecraft.getMinecraft().player.getHeldItemMainhand();
            if (stack.getItem() instanceof ItemTelegraphKey == false) return;

            if (keyMorse.isKeyDown() && !wasPressed) {
                long now = System.currentTimeMillis();
                if (now - lastClick > 500) {
                    if (bufferIndex > 0) {
                        String letter = decodeMorse();
                        if (!letter.isEmpty()) {
                            AudioPacketHandler.sendMorseMessage(letter, Minecraft.getMinecraft().player);
                            Minecraft.getMinecraft().player.sendMessage(
                                    new net.minecraft.util.text.TextComponentString("§7[Морзе] §f" + letter)
                            );
                        }
                        bufferIndex = 0;
                    }
                }
                if (bufferIndex < morseBuffer.length) {
                    morseBuffer[bufferIndex++] = true;
                }
                lastClick = now;
                wasPressed = true;
            } else if (!keyMorse.isKeyDown() && wasPressed) {
                wasPressed = false;
            }
        }
    }
}