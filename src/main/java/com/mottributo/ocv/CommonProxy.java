package com.mottributo.ocv;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        OCV.LOG.info("I am Orthogonal Camera View at version " + Tags.VERSION);
    }
}
