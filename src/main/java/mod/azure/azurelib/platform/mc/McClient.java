package mod.azure.azurelib.platform.mc;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.item.BlockItemForm;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public final class McClient {

    private static Minecraft minecraft;

    private McClient() {
        throw new UnsupportedOperationException();
    }

    public static Minecraft mc() {
        Minecraft mc = minecraft;
        if (mc == null) {
            mc = (Minecraft) FabricLoader.getInstance().getGameInstance();
            minecraft = mc;
        }
        return mc;
    }

    @Nullable
    public static World world() {
        Minecraft mc = mc();
        return mc == null ? null : mc.world;
    }

    @Nullable
    public static PlayerEntity player() {
        Minecraft mc = mc();
        return mc == null ? null : mc.player;
    }

    @Nullable
    public static Entity cameraEntity() {
        Minecraft mc = mc();
        if (mc == null)
            return null;
        return mc.camera != null ? mc.camera : mc.player;
    }

    public static boolean isLocalWorld() {
        World world = world();
        return world != null && !Mc.isClientSide(world);
    }

    public static boolean isPaused() {
        Minecraft mc = mc();
        return mc != null && mc.paused;
    }

    public static int renderDistanceChunks() {
        Minecraft mc = mc();
        if (mc == null || mc.options == null)
            return 8;
        return 16 >> Math.max(0, Math.min(3, mc.options.viewDistance));
    }

    public static int moonPhase() {
        World world = world();
        return world == null ? 0 : (int) (Mc.time(world) / 24000L % 8L + 8L) % 8;
    }

    public static TextRenderer textRenderer() {
        return mc().textRenderer;
    }

    public static EntityRenderDispatcher entityRenderDispatcher() {
        return EntityRenderDispatcher.INSTANCE;
    }

    public static float cameraYaw() {
        return EntityRenderDispatcher.INSTANCE.yaw;
    }

    public static float cameraPitch() {
        return EntityRenderDispatcher.INSTANCE.pitch;
    }

    public static double distanceToCameraSq(double x, double y, double z) {
        Entity camera = cameraEntity();
        if (camera == null)
            return 0;
        double dx = Mc.x(camera) - x;
        double dy = Mc.y(camera) - y;
        double dz = Mc.z(camera) - z;
        return dx * dx + dy * dy + dz * dz;
    }

    public static boolean isFirstPersonCamera(Entity entity) {
        Minecraft mc = mc();
        return mc != null && mc.options != null && !mc.options.thirdPerson && entity == cameraEntity();
    }

    public static long systemTimeMillis() {
        return System.currentTimeMillis();
    }

    public static void setPivot(ModelPart part, float x, float y, float z) {
        part.setPivot(x, y, z);
    }

    public static void setAngles(
        BipedEntityModel model,
        float limbSwing,
        float limbSwingAmount,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        float scale
    ) {
        model.setAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }

    public static void setBipedPose(BipedEntityModel model, boolean sneaking, boolean holdingItem, float swing) {
        model.sneaking = sneaking;
        model.rightArmPose = holdingItem;
        model.handSwingProgress = swing;
    }

    public static void copyBipedPose(BipedEntityModel from, BipedEntityModel to) {
        to.sneaking = from.sneaking;
        to.rightArmPose = from.rightArmPose;
        to.leftArmPose = from.leftArmPose;
        to.handSwingProgress = from.handSwingProgress;
        to.riding = from.riding;
    }

    public static void renderVanillaItem(LivingEntity holder, ItemStack stack) {
        LivingEntity entity = holder != null ? holder : player();
        if (entity == null || stack == null)
            return;
        EntityRenderDispatcher.INSTANCE.heldItemRenderer.renderItem(entity, stack);
    }

    public static void bindPlayerSkin(PlayerEntity player) {
        Minecraft mc = mc();
        int id = mc.textureManager.downloadTexture(player.skinUrl, "/mob/char.png");
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }

    public static boolean isBlockItem3D(ItemStack stack) {
        Object item = Mc.item(stack);
        if (!(item instanceof BlockItemForm)) {
            return false;
        }
        Block block = ((BlockItemForm) item).getBlock();
        return block != null && BlockRenderManager.isSideLit(block.getRenderType());
    }
}
