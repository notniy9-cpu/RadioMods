package com.Radio.RadioMod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.sound.sampled.*;
import java.util.Random;

import com.Radio.RadioMod.RadioMods;
import com.Radio.RadioMod.blocks.BlockBooster;

public class AudioPacketHandler {
    private static TargetDataLine mic;
    private static AudioFormat format;
    private static int currentFreq;
    private static EntityPlayer currentSpeaker;
    private static boolean recording = false;

    public static void register() {
        RadioMods.NETWORK.registerMessage(AudioMessageHandler.class, AudioMessage.class, 0, Side.CLIENT);
        RadioMods.NETWORK.registerMessage(RecordingStartHandler.class, RecordingStartMessage.class, 1, Side.SERVER);
        RadioMods.NETWORK.registerMessage(MorseMessageHandler.class, MorseMessage.class, 2, Side.CLIENT);
    }

    public static void startRecording(int freq, EntityPlayer player) {
        if (recording) return;
        currentFreq = freq;
        currentSpeaker = player;

        format = new AudioFormat(16000, 16, 1, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        try {
            mic = (TargetDataLine) AudioSystem.getLine(info);
            mic.open(format);
            mic.start();
            recording = true;

            RadioMods.NETWORK.sendToServer(new RecordingStartMessage(freq));

            new Thread(() -> {
                byte[] buffer = new byte[240];
                while (recording && mic != null) {
                    int read = mic.read(buffer, 0, buffer.length);
                    if (read > 0) {
                        sendAudioPacket(buffer, freq, player);
                    }
                }
            }).start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static void stopRecording() {
        if (mic != null) {
            mic.stop();
            mic.close();
            mic = null;
        }
        recording = false;
        RadioMods.NETWORK.sendToServer(new RecordingStartMessage(-1));
    }

    private static void sendAudioPacket(byte[] data, int freq, EntityPlayer speaker) {
        double range = calculateRange(speaker, freq);
        for (EntityPlayer player : speaker.world.playerEntities) {
            if (player == speaker) continue;
            double dist = speaker.getDistance(player);
            if (dist <= range) {
                boolean glitch = dist > range * 0.9;
                RadioMods.NETWORK.sendTo(new AudioMessage(data, freq, glitch), (EntityPlayerMP)player);
            }
        }
    }

    private static double calculateRange(EntityPlayer speaker, int freq) {
        double baseRange = 100.0;

        float boosterMult = BlockBooster.getTotalMultiplier(speaker.world, speaker.getPosition());
        baseRange *= boosterMult;

        Biome biome = speaker.world.getBiome(speaker.getPosition());
        String biomeName = biome.getBiomeName();

        if (biomeName != null) {
            if (biomeName.contains("Hills") || biomeName.contains("Mountains") || biomeName.contains("Extreme")) {
                baseRange *= 1.2;
            }

            if (biomeName.contains("Ocean") || biomeName.contains("River") || biomeName.contains("Beach")) {
                baseRange *= 0.2;
            }
        }

        if (speaker.posY < 40 && speaker.world.getLightFromNeighbors(speaker.getPosition()) < 7) {
            baseRange *= 0.5;
        }

        if (speaker.isInWater()) {
            baseRange *= 0.2;
        }

        return Math.max(15.0, baseRange);
    }

    public static void sendMorseMessage(String letter, EntityPlayer speaker) {
        for (EntityPlayer player : speaker.world.playerEntities) {
            if (player == speaker) continue;
            double range = calculateRange(speaker, 0);
            if (speaker.getDistance(player) <= range) {
                RadioMods.NETWORK.sendTo(new MorseMessage(letter), (EntityPlayerMP)player);
            }
        }
    }

    // ========== Классы сообщений ==========

    public static class AudioMessage implements IMessage {
        byte[] data;
        int freq;
        boolean glitch;
        public AudioMessage() {}
        public AudioMessage(byte[] d, int f, boolean g) { data = d; freq = f; glitch = g; }

        @Override public void fromBytes(ByteBuf buf) {
            data = new byte[buf.readableBytes() - 5];
            buf.readBytes(data);
            freq = buf.readInt();
            glitch = buf.readBoolean();
        }
        @Override public void toBytes(ByteBuf buf) {
            buf.writeBytes(data);
            buf.writeInt(freq);
            buf.writeBoolean(glitch);
        }
    }

    public static class AudioMessageHandler implements IMessageHandler<AudioMessage, IMessage> {
        @SideOnly(Side.CLIENT)
        @Override public IMessage onMessage(AudioMessage msg, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                if (msg.glitch && new Random().nextFloat() < 0.3f) return;
                playAudio(msg.data);
            });
            return null;
        }

        @SideOnly(Side.CLIENT)
        private void playAudio(byte[] data) {
            try {
                AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
                line.write(data, 0, data.length);
                line.drain();
                line.close();
            } catch (LineUnavailableException e) {}
        }
    }

    public static class RecordingStartMessage implements IMessage {
        int freq;
        public RecordingStartMessage() {}
        public RecordingStartMessage(int f) { freq = f; }
        @Override public void fromBytes(ByteBuf buf) { freq = buf.readInt(); }
        @Override public void toBytes(ByteBuf buf) { buf.writeInt(freq); }
    }

    public static class RecordingStartHandler implements IMessageHandler<RecordingStartMessage, IMessage> {
        @Override public IMessage onMessage(RecordingStartMessage msg, MessageContext ctx) {
            return null;
        }
    }

    public static class MorseMessage implements IMessage {
        String text;
        public MorseMessage() {}
        public MorseMessage(String t) { text = t; }
        @Override public void fromBytes(ByteBuf buf) {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            text = new String(bytes);
        }
        @Override public void toBytes(ByteBuf buf) {
            buf.writeBytes(text.getBytes());
        }
    }

    public static class MorseMessageHandler implements IMessageHandler<MorseMessage, IMessage> {
        @SideOnly(Side.CLIENT)
        @Override public IMessage onMessage(MorseMessage msg, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Minecraft.getMinecraft().player.sendMessage(
                        new net.minecraft.util.text.TextComponentString("§7[Морзе] §f" + msg.text)
                );
            });
            return null;
        }
    }
}