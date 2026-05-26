package com.Radio.RadioMod.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import com.Radio.RadioMod.RadioMods;
import com.Radio.RadioMod.gui.GuiRadio;
import com.Radio.RadioMod.network.AudioPacketHandler;

public class ItemRadio extends Item {
    public static KeyBinding keyPTT;
    private static boolean wasPressed = false;

    public ItemRadio() {
        setRegistryName("radio");
        setUnlocalizedName("radio");
        setCreativeTab(net.minecraft.creativetab.CreativeTabs.TOOLS);
        setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    public static void initKey() {
        keyPTT = new KeyBinding("key.radio.ptt", Keyboard.KEY_V, "key.categories.radio");
        ClientRegistry.registerKeyBinding(keyPTT);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            player.openGui(RadioMods.MODID, 0, world, (int)player.posX, (int)player.posY, (int)player.posZ);
        }
        return super.onItemRightClick(world, player, hand);
    }

    @SideOnly(Side.CLIENT)
    public static class ClientTick {
        private static boolean isRecording = false;

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            if (Minecraft.getMinecraft().player == null) return;

            ItemStack stack = Minecraft.getMinecraft().player.getHeldItemMainhand();
            if (stack.getItem() instanceof ItemRadio == false) {
                stack = Minecraft.getMinecraft().player.getHeldItemOffhand();
                if (stack.getItem() instanceof ItemRadio == false) return;
            }

            if (keyPTT != null && keyPTT.isPressed() && !wasPressed) {
                startRecording(stack);
                wasPressed = true;
            } else if (keyPTT != null && !keyPTT.isKeyDown() && wasPressed) {
                stopRecording();
                wasPressed = false;
            }
        }

        private static void startRecording(ItemStack stack) {
            if (isRecording) return;
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null) {
                nbt = new NBTTagCompound();
                stack.setTagCompound(nbt);
            }
            int freq = nbt.getInteger("frequency");
            AudioPacketHandler.startRecording(freq, Minecraft.getMinecraft().player);
            isRecording = true;
        }

        private static void stopRecording() {
            if (!isRecording) return;
            AudioPacketHandler.stopRecording();
            isRecording = false;
        }
    }
}