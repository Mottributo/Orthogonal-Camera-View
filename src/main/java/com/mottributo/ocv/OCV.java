package com.mottributo.ocv;

import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = OCV.MODID, version = Tags.VERSION, name = "Orthogonal Camera View", acceptedMinecraftVersions = "[1.7.10]")
public class OCV {

    public static final String MODID = "ocv";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = "com.mottributo.ocv.ClientProxy", serverSide = "com.mottributo.ocv.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry."
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        if (event.getSide() == Side.CLIENT) {
            OrthoHandler orthoHandler = new OrthoHandler();
            FMLCommonHandler.instance()
                .bus()
                .register(orthoHandler);
            MinecraftForge.EVENT_BUS.register(orthoHandler);
        }
    }
}
