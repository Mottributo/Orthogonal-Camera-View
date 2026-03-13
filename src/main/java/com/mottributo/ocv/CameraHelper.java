/**
 * This module is needed to read private camera values.
 **/

package com.mottributo.ocv;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;

import cpw.mods.fml.relauncher.ReflectionHelper;

public class CameraHelper {

    private static final String[] cameraZoom = { "cameraZoom", "field_78503_V" };
    private static final String[] cameraYaw = { "cameraYaw", "field_78502_W" };
    private static final String[] cameraPitch = { "cameraPitch", "field_78509_X" };

    public static double getCameraZoom() {
        EntityRenderer renderer = Minecraft.getMinecraft().entityRenderer;
        return ReflectionHelper.getPrivateValue(EntityRenderer.class, renderer, cameraZoom);
    }

    public static double getCameraYaw() {
        EntityRenderer renderer = Minecraft.getMinecraft().entityRenderer;
        return ReflectionHelper.getPrivateValue(EntityRenderer.class, renderer, cameraYaw);
    }

    public static double getCameraPitch() {
        EntityRenderer renderer = Minecraft.getMinecraft().entityRenderer;
        return ReflectionHelper.getPrivateValue(EntityRenderer.class, renderer, cameraPitch);
    }
}
