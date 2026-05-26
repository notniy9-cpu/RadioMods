package com.Radio.RadioMod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiRadio extends GuiScreen {
    private ItemStack radio;
    private int frequency = 0;
    private int[] colors = {0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0xFF00FF, 0x00FFFF, 0xFFA500, 0x800080, 0xFFC0CB, 0x808080};

    public GuiRadio(ItemStack stack) {
        this.radio = stack;
        if (stack.getTagCompound() != null) {
            frequency = stack.getTagCompound().getInteger("frequency");
        }
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        this.buttonList.add(new GuiSlider(0, width/2 - 100, height/2 - 20, 200, 20,
                "Частота: ", " Hz", 0, 9, frequency, false, true,
                slider -> {
                    frequency = (int)slider.getValue();
                    NBTTagCompound nbt = radio.getTagCompound();
                    if (nbt == null) nbt = new NBTTagCompound();
                    nbt.setInteger("frequency", frequency);
                    radio.setTagCompound(nbt);
                }));

        for (int i = 0; i < colors.length; i++) {
            int x = width/2 - 100 + (i % 5) * 40;
            int y = height/2 + 30 + (i / 5) * 30;
            this.buttonList.add(new ColorButton(i, x, y, 30, 20, colors[i], i));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button instanceof ColorButton) {
            frequency = ((ColorButton)button).freq;
            NBTTagCompound nbt = radio.getTagCompound();
            if (nbt == null) nbt = new NBTTagCompound();
            nbt.setInteger("frequency", frequency);
            radio.setTagCompound(nbt);

            for (GuiButton btn : buttonList) {
                if (btn instanceof GuiSlider) {
                    ((GuiSlider)btn).setValue(frequency);
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(fontRenderer, "Настройка рации", width/2, height/2 - 50, 0xFFFFFF);
        this.drawCenteredString(fontRenderer, "Текущая частота: " + frequency, width/2, height/2 - 35, 0xAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private class ColorButton extends GuiButton {
        public int freq;
        private int color;

        public ColorButton(int id, int x, int y, int w, int h, int col, int f) {
            super(id, x, y, w, h, "");
            this.color = col;
            this.freq = f;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partial) {
            GlStateManager.color(1, 1, 1, 1);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            drawRect(x, y, x+width, y+height, color);
            if (hovered) {
                drawRect(x, y, x+width, y+height, 0x88FFFFFF);
            }
            drawCenteredString(mc.fontRenderer, String.valueOf(freq), x+width/2, y+height/2-4, 0xFFFFFF);
        }
    }
}
