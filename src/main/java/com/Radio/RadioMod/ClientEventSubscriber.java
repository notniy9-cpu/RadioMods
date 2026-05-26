package com.Radio.RadioMod;

import com.Radio.RadioMod.items.ItemRadio;
import com.Radio.RadioMod.items.ItemTelegraphKey;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientEventSubscriber {

    public static void preInit(FMLPreInitializationEvent event) {
        ItemRadio.initKey();
        ItemTelegraphKey.initKey();
    }

    public static void init(FMLInitializationEvent event) {
        // Регистрируем клиентские тики
        MinecraftForge.EVENT_BUS.register(ItemRadio.ClientTick.class);
        MinecraftForge.EVENT_BUS.register(ItemTelegraphKey.ClientTick.class);
    }
}
