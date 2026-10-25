package mod.azure.azurelib.platform.mc;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.model.ModelPart;
import net.minecraft.client.render.model.entity.HumanoidModel;
import net.minecraft.client.render.texture.DynamicTexture;
import net.minecraft.client.render.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

import mod.azure.azurelib.mixins.client.accessor.BlockEntityRenderDispatcherAccessor;
import mod.azure.azurelib.mixins.client.accessor.EntityRenderDispatcherAccessor;

/**
 * Client-side counterpart of {@link Mc}: the single place AzureLib touches the game instance, the render dispatcher and
 * vanilla model parts. Member accesses are annotated with their MCP (b1.7.3) names for checking against Feather.
 */
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

    public static int textWidth(TextRenderer font, String text) {
        return font.getWidth(text);
    }

    public static void drawText(TextRenderer font, String text, int x, int y, int color) {
        font.draw(text, x, y, color);
    }

    /**
     * Puts a renderer in the entity dispatcher's map and gives it the dispatcher, exactly as the dispatcher's
     * constructor does for vanilla renderers.
     */
    @SuppressWarnings("unchecked")
    public static void installEntityRenderer(
        EntityRenderDispatcher dispatcher,
        Class<? extends Entity> entityClass,
        EntityRenderer renderer
    ) {
        ((EntityRenderDispatcherAccessor) dispatcher).azurelib$getRenderers().put(entityClass, renderer);
        renderer.init(dispatcher);
    }

    /**
     * Puts a renderer in the block entity dispatcher's map and gives it the dispatcher, exactly as the dispatcher's
     * constructor does for vanilla renderers.
     */
    @SuppressWarnings("unchecked")
    public static void installBlockEntityRenderer(
        BlockEntityRenderDispatcher dispatcher,
        Class<? extends BlockEntity> blockEntityClass,
        BlockEntityRenderer renderer
    ) {
        ((BlockEntityRenderDispatcherAccessor) dispatcher).azurelib$getRenderers().put(blockEntityClass, renderer);
        renderer.init(dispatcher);
    }

    /**
     * The renderer the dispatcher would use for this entity, found the way {@code getEntityRenderObject} does (walking
     * up the class hierarchy) through the accessor, so no dispatcher method name is involved.
     */
    @Nullable
    public static EntityRenderer entityRenderer(Entity entity) {
        java.util.Map<?, ?> renderers = ((EntityRenderDispatcherAccessor) EntityRenderDispatcher.INSTANCE)
            .azurelib$getRenderers();
        for (Class<?> type = entity.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            Object renderer = renderers.get(type);
            if (renderer instanceof EntityRenderer) {
                return (EntityRenderer) renderer;
            }
        }
        return null;
    }

    public static EntityRenderDispatcher entityRenderDispatcher() {
        return EntityRenderDispatcher.INSTANCE;
    }

    public static float cameraYaw() {
        return EntityRenderDispatcher.INSTANCE.cameraYaw;
    }

    public static float cameraPitch() {
        return EntityRenderDispatcher.INSTANCE.cameraPitch;
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
        return mc != null && mc.options != null && !mc.options.perspective && entity == cameraEntity();
    }

    public static long systemTimeMillis() {
        return System.currentTimeMillis();
    }

    public static float pivotX(ModelPart part) {
        return part.x;
    }

    public static float pivotY(ModelPart part) {
        return part.y;
    }

    public static float pivotZ(ModelPart part) {
        return part.z;
    }

    public static float rotX(ModelPart part) {
        return part.rotationX;
    }

    public static float rotY(ModelPart part) {
        return part.rotationY;
    }

    public static float rotZ(ModelPart part) {
        return part.rotationZ;
    }

    public static void setRotX(ModelPart part, float value) {
        part.rotationX = value;
    }

    public static void setRotY(ModelPart part, float value) {
        part.rotationY = value;
    }

    public static void setRotZ(ModelPart part, float value) {
        part.rotationZ = value;
    }

    public static void setRotation(ModelPart part, float x, float y, float z) {
        setRotX(part, x);
        setRotY(part, y);
        setRotZ(part, z);
    }

    public static void setVisible(ModelPart part, boolean visible) {
        part.visible = visible;
    }

    public static void renderPart(ModelPart part, float scale) {
        part.render(scale);
    }

    public static ModelPart bipedHead(HumanoidModel model) {
        return model.head;
    }

    public static ModelPart bipedHat(HumanoidModel model) {
        return model.hat;
    }

    public static ModelPart bipedBody(HumanoidModel model) {
        return model.body;
    }

    public static ModelPart bipedRightArm(HumanoidModel model) {
        return model.rightArm;
    }

    public static ModelPart bipedLeftArm(HumanoidModel model) {
        return model.leftArm;
    }

    public static ModelPart bipedRightLeg(HumanoidModel model) {
        return model.rightLeg;
    }

    public static ModelPart bipedLeftLeg(HumanoidModel model) {
        return model.leftLeg;
    }

    public static void setPivot(ModelPart part, float x, float y, float z) {
        part.setPos(x, y, z);
    }

    public static void setAngles(
        HumanoidModel model,
        float limbSwing,
        float limbSwingAmount,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        float scale
    ) {
        model.setupAnimation(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }

    public static void setBipedPose(HumanoidModel model, boolean sneaking, boolean holdingItem, float swing) {
        model.sneaking = sneaking;
        model.itemInRightHand = holdingItem;
        model.attackAnimationProgress = swing;
    }

    public static void copyBipedPose(HumanoidModel from, HumanoidModel to) {
        to.sneaking = from.sneaking;
        to.itemInRightHand = from.itemInRightHand;
        to.itemInLeftHand = from.itemInLeftHand;
        to.attackAnimationProgress = from.attackAnimationProgress;
        to.riding = from.riding;
    }

    public static void renderVanillaItem(MobEntity holder, ItemStack stack) {
        MobEntity entity = holder != null ? holder : player();
        if (entity == null || stack == null)
            return;
        EntityRenderDispatcher.INSTANCE.itemInHandRenderer.render(entity, stack);
    }

    public static void bindPlayerSkin(PlayerEntity player) {
        Minecraft mc = mc();
        int id = mc.textureManager.loadHttpTexture(player.httpTexture1, "/mob/char.png");
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }

    /**
     * Mirrors Beta's own held-item check: item ids below 256 are blocks, and only blocks whose render type the block
     * renderer can draw as a cube are rendered in 3D.
     */
    // ---- Item icons (see AzItemIcons) ----

    /** Every item by id; ids below 256 are block items. MCP: Item.itemsList */
    public static Item[] itemsById() {
        return Item.BY_ID;
    }

    /** The item's icon index in /gui/items.png for a damage value. MCP: Item.getIconFromDamage */
    public static int itemSprite(Item item, int damage) {
        return item.getSprite(damage);
    }

    /** Points the item at an icon index in /gui/items.png. MCP: Item.setIconIndex */
    public static void setItemSprite(Item item, int sprite) {
        item.setSprite(sprite);
    }

    /** Registers a dynamic texture, uploaded into its sheet every tick. MCP: RenderEngine.registerTextureFX */
    public static void addDynamicTexture(
        TextureManager textureManager,
        DynamicTexture texture
    ) {
        textureManager.addDynamicTexture(texture);
    }

    public static boolean isBlockItem3D(ItemStack stack) {
        Item item = Mc.item(stack);
        if (item == null) {
            return false;
        }
        int id = Mc.itemId(item);
        if (id < 0 || id >= 256) {
            return false;
        }
        Block block = Block.BY_ID[id];
        return block != null && BlockRenderer.isItem3d(block.getRenderType());
    }
}
