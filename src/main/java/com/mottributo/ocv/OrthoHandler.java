package com.mottributo.ocv;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

// TODO Add it as a 4th camera view mode instead of an addon triggered by pressing numpad5.
// TODO Complete fullscreen world edit functionality.
// So far it feels slightly compressed, perhaps it raycasts using the regular projection?..
// TODO a properly done Modrinth branch that only forbids using the camera in multiplayer.

public class OrthoHandler {

    /**
     * Misc debug data in the F3 menu. TODO expose to config file.
     */
    public static final boolean debugMode = true;
    private static final String KEY_CATEGORY = "Orthogonal Camera View";
    private static final float ZOOM_STEP = 0.5f;
    private static final float ROTATE_STEP = 15;
    // TODO check are there mods which make this variable, and ensure integration with these.
    private static final float SECONDS_PER_TICK = 1f / 20f;

    private final KeyBinding keyToggle = new KeyBinding("Toggle", Keyboard.KEY_NUMPAD5, KEY_CATEGORY);
    private final KeyBinding keyZoomIn = new KeyBinding("Zoom in", Keyboard.KEY_ADD, KEY_CATEGORY);
    private final KeyBinding keyZoomOut = new KeyBinding("Zoom out", Keyboard.KEY_SUBTRACT, KEY_CATEGORY);
    private final KeyBinding keyRotateL = new KeyBinding("Rotate left", Keyboard.KEY_NUMPAD4, KEY_CATEGORY);
    private final KeyBinding keyRotateR = new KeyBinding("Rotate right", Keyboard.KEY_NUMPAD6, KEY_CATEGORY);
    private final KeyBinding keyRotateU = new KeyBinding("Rotate up", Keyboard.KEY_NUMPAD8, KEY_CATEGORY);
    private final KeyBinding keyRotateD = new KeyBinding("Rotate down", Keyboard.KEY_NUMPAD2, KEY_CATEGORY);
    private final KeyBinding keyRotateT = new KeyBinding("Look from top", Keyboard.KEY_NUMPAD7, KEY_CATEGORY);
    private final KeyBinding keyRotateF = new KeyBinding("Look from front", Keyboard.KEY_NUMPAD1, KEY_CATEGORY);
    private final KeyBinding keyRotateS = new KeyBinding("Look from side", Keyboard.KEY_NUMPAD3, KEY_CATEGORY);
    private final KeyBinding keyRotateC = new KeyBinding("Look from corner", Keyboard.KEY_NUMPAD9, KEY_CATEGORY);
    private final KeyBinding keyClip = new KeyBinding("Clip terrain", Keyboard.KEY_MULTIPLY, KEY_CATEGORY);
    private final KeyBinding keyTether = new KeyBinding("Free/tethered cam toggle", Keyboard.KEY_DIVIDE, KEY_CATEGORY);

    /** Whether the orthogonal camera mode is on. */
    private boolean isEnabled;
    /**
     * Whether the cam's angle is tethered to the player's angle. True when tethered.
     * If false and the orthogonal view is enabled,
     * the player can manipulate the world from any point visible from the viewport.
     **/
    private boolean isCamTethered;
    /** Whether to remove all geometry between the camera and the player. Crappy means of seeing through buildings. */
    private boolean isClipping;

    private float zoom;
    private float xRot;
    private float yRot;

    private int tick;
    private int tickPrevious;
    private double partialPrevious;

    public OrthoHandler() {
        ClientRegistry.registerKeyBinding(keyToggle);
        ClientRegistry.registerKeyBinding(keyZoomIn);
        ClientRegistry.registerKeyBinding(keyZoomOut);
        ClientRegistry.registerKeyBinding(keyRotateL);
        ClientRegistry.registerKeyBinding(keyRotateR);
        ClientRegistry.registerKeyBinding(keyRotateU);
        ClientRegistry.registerKeyBinding(keyRotateD);
        ClientRegistry.registerKeyBinding(keyRotateT);
        ClientRegistry.registerKeyBinding(keyRotateF);
        ClientRegistry.registerKeyBinding(keyRotateS);
        ClientRegistry.registerKeyBinding(keyRotateC);
        ClientRegistry.registerKeyBinding(keyClip);
        ClientRegistry.registerKeyBinding(keyTether);

        reset();
    }

    private void reset() {
        isCamTethered = false;
        isClipping = false;

        zoom = 8;
        // These two values shouldn't be used in game, it's just that
        // reset() in pre-initialization Forge stage can't reference Minecraft
        xRot = 30; // Replaced by mc.thePlayer.rotationPitch in toggle()
        yRot = 45; // Replaced by mc.thePlayer.rotationYaw in toggle()
        tick = 0;
        tickPrevious = 0;
        partialPrevious = 0;
    }

    public void toggle() {
        if (isEnabled) { // Disable
            Minecraft mc = Minecraft.getMinecraft();
            mc.mouseHelper.grabMouseCursor();
            isEnabled = false;
        } else { // Enable
            reset();
            Minecraft mc = Minecraft.getMinecraft();
            xRot = mc.thePlayer.rotationPitch;
            yRot = mc.thePlayer.rotationYaw;
            isEnabled = true;
        }
    }

    private boolean modifierKeyPressed() {
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent evt) {
        boolean mod = modifierKeyPressed();
        Minecraft mc = Minecraft.getMinecraft();

        if (keyToggle.isPressed()) {
            toggle();
        } else if (isEnabled) {
            if (keyClip.isPressed()) {
                isClipping = !isClipping;
            } else if (keyTether.isPressed()) {
                isCamTethered = !isCamTethered;
                if (isCamTethered) { // recover the grab on tethering the camera
                    mc.mouseHelper.grabMouseCursor();
                }
            } else if (keyRotateC.isPressed()) {
                xRot = 30;
                yRot = mod ? -45 : 45;
            } else if (keyRotateT.isPressed()) {
                xRot = mod ? -90 : 90;
                yRot = 0;
            } else if (keyRotateF.isPressed()) {
                xRot = 0;
                yRot = mod ? -90 : 90;
            } else if (keyRotateS.isPressed()) {
                xRot = 0;
                yRot = mod ? 180 : 0;
            }

            if (mod) {
                // snap values to step units
                xRot -= xRot % ROTATE_STEP;
                yRot -= yRot % ROTATE_STEP;
                zoom -= zoom % ZOOM_STEP;

                updateZoomAndRotation(1);
            }
        }
    }

    private void updateZoomAndRotation(double multi) {
        if (keyZoomIn.getIsKeyPressed()) {
            zoom *= 1 - ZOOM_STEP * multi;
        } else if (keyZoomOut.getIsKeyPressed()) {
            zoom *= 1 + ZOOM_STEP * multi;
        }

        if (keyRotateL.getIsKeyPressed()) {
            yRot += ROTATE_STEP * multi;
        } else if (keyRotateR.getIsKeyPressed()) {
            yRot -= ROTATE_STEP * multi;
        }

        if (keyRotateU.getIsKeyPressed()) {
            xRot += ROTATE_STEP * multi;
        } else if (keyRotateD.getIsKeyPressed()) {
            xRot -= ROTATE_STEP * multi;
        }
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent evt) {
        if (!isEnabled || evt.phase != Phase.START) {
            return;
        }
        tick++;
    }

    // This seems to fire every tick while the ortho mode is enabled.
    // Allegedly, before the fog density is calculated - this keeps fog relative to the player's position.
    @SubscribeEvent
    public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (!isEnabled) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        // Releases the cursor for whole viewport world edit purposes.
        if (Mouse.isGrabbed() && !isCamTethered) {
            mc.mouseHelper.ungrabMouseCursor();
        }

        // update zoom and rotation
        if (!modifierKeyPressed()) {
            int ticksElapsed = tick - tickPrevious;
            double elapsed = ticksElapsed + (event.renderPartialTicks - partialPrevious);
            elapsed *= SECONDS_PER_TICK;
            updateZoomAndRotation(elapsed);

            tickPrevious = tick;
            partialPrevious = event.renderPartialTicks;
        }

        if (!isCamTethered) {
            // Tethers the player's head direction to the camera's. So WASD movement is aligned with the cam.
            mc.thePlayer.rotationPitch = xRot;
            mc.thePlayer.rotationYaw = yRot;
        }

        float width = zoom * (mc.displayWidth / (float) mc.displayHeight);
        float height = zoom;

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();

        double cameraZoom = CameraHelper.getCameraZoom();
        double cameraOfsX = CameraHelper.getCameraYaw();
        double cameraOfsY = CameraHelper.getCameraPitch();

        if (cameraZoom != 1) {
            GL11.glTranslated(cameraOfsX, -cameraOfsY, 0);
            GL11.glScaled(cameraZoom, cameraZoom, 1);
        }

        GL11.glOrtho(-width, width, -height, height, isClipping ? 0 : -9999, 9999);

        if (isCamTethered) {
            // rotate the orthographic camera with the player view
            xRot = mc.thePlayer.rotationPitch;
            yRot = mc.thePlayer.rotationYaw;
        }

        // set camera rotation
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glRotatef(xRot, 1, 0, 0);
        GL11.glRotatef(yRot + 180.0F, 0, 1, 0);

        if (!isCamTethered) {
            // [ This fixes particle rotation.
            float pitch = xRot;
            float yaw = yRot;
            ActiveRenderInfo.rotationX = MathHelper.cos(yaw * (float) Math.PI / 180f);
            ActiveRenderInfo.rotationZ = MathHelper.sin(yaw * (float) Math.PI / 180f);
            ActiveRenderInfo.rotationYZ = -ActiveRenderInfo.rotationZ * MathHelper.sin(pitch * (float) Math.PI / 180f);
            ActiveRenderInfo.rotationXY = ActiveRenderInfo.rotationX * MathHelper.sin(pitch * (float) Math.PI / 180f);
            ActiveRenderInfo.rotationXZ = MathHelper.cos(pitch * (float) Math.PI / 180f);
            // ]

            getOrthoMouseOver(mc, (float) event.renderPartialTicks);

        }

    }

    // Determines how to edit the world given the free mouse.
    // TODO total rewrite with a mixin to override the original block selection code.
    // The original block selection procedure converts player position data and player head rotation data
    // to two block coordinates,
    private void getOrthoMouseOver(Minecraft mc, float partialTicks) {
        // Do nothing if nothing to render?
        if (mc.renderViewEntity == null) return;
        // Do nothing if no world yet to edit.
        if (mc.theWorld == null) return;

        float width = zoom * (mc.displayWidth / (float) mc.displayHeight);
        float height = zoom * (mc.displayHeight / (float) mc.displayWidth);

        // normalize mouse to -1..1

        float mx = ((float) Mouse.getX() / mc.displayWidth - 0.5F) * 2.0F;
        float my = ((float) Mouse.getY() / mc.displayHeight - 0.5F) * 2.0F;
        // The fuck this does?
        float rotate_z = MathHelper.cos(-yRot * 0.017453292F - (float) Math.PI);
        float rotate_x = MathHelper.sin(-yRot * 0.017453292F - (float) Math.PI);
        float rotate_xz = -MathHelper.cos(-xRot * 0.017453292F);
        float rotate_y = MathHelper.sin(-xRot * 0.017453292F);

        Vec3 look = Vec3.createVectorHelper((double) (rotate_x * rotate_xz), (double) rotate_y, (double) (rotate_z * rotate_xz));
        Vec3 pos = mc.renderViewEntity.getPosition(partialTicks);

        // Move to mouse position
        Vec3 from = pos.addVector((double) (-rotate_z * rotate_xz) * mx * width, 0, (double) (rotate_x * rotate_xz) * mx * height);
        Vec3 to = from.addVector(look.xCoord * 16, look.yCoord * 16, look.zCoord * 16);

        mc.pointedEntity = null;
        mc.objectMouseOver = mc.theWorld.rayTraceBlocks(from, to);

    }

    @SubscribeEvent
    public void onMouseEvent(MouseEvent event) {
        if (!isEnabled) { // Do nothing if the ortho cam is not enabled
            return;
        }
        if (!isCamTethered) {
            // If the camera's angle isn't dependent on the player angle and thus on the mouse movement
            getOrthoMouseOver(Minecraft.getMinecraft(), 1.0F);
        }
    }

    @SubscribeEvent
    // Debug stuff.
    public void onDebugOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!mc.gameSettings.showDebugInfo) return;
        if (debugMode) {
            event.right.add("----OCV DEBUG MODE ON----");
            event.right.add("isEnabled: " + isEnabled);
            event.right.add("zoom: " + zoom);
            event.right.add("xRot, yRot: " + xRot + ", " + yRot);
            event.right.add(mc.displayWidth + "-" + mc.displayHeight);
            for (int key = 0; key < Keyboard.KEYBOARD_SIZE; key++) {
                if (Keyboard.isKeyDown(key)) {
                    String keyName = Keyboard.getKeyName(key);
                    event.right.add("Pressed: " + keyName);
                }
            }
        }
    }

}
