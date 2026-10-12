package mod.azure.azurelib.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import mod.azure.azurelib.core.object.Color;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * An abstract base class representing the rendering context for a custom rendering pipeline. This class provides
 * generic rendering properties and behavior that can be extended to customize rendering for different types of
 * animatable objects.
 *
 * @param <K> The type of the key used to identify the animatable object. Typically, a UUID for items/entities and Long
 *            for BlockEntities.
 * @param <T> the type of the animatable object being rendered
 */
public abstract class AzRendererPipelineContext<K, T> {

    public Identifier textureOverride;

    private final AzRendererPipeline<K, T> rendererPipeline;

    protected T animatable;

    protected @Nullable Entity currentEntity;

    private AzBakedModel bakedModel;

    private AzBufferSource multiBufferSource;

    private int packedLight;

    private int packedOverlay;

    private float partialTick;

    private PoseStack poseStack;

    private int renderColor;

    private @Nullable RenderType renderType;

    private VertexConsumer vertexConsumer;

    private boolean applyAnimationOnReRender;

    private boolean inModelPass;

    private @Nullable RenderType modelPassRenderType;

    private int modelPassStart;

    private int modelPassEnd;

    private int modelPassTotalStart;

    private int modelPassLayerVertices;

    private boolean modelPassMirrorable;

    protected static final Map<Identifier, IntIntPair> TEXTURE_DIMENSIONS_CACHE =
        new Object2ObjectOpenHashMap<>();

    protected AzRendererPipelineContext(AzRendererPipeline<K, T> rendererPipeline) {
        this.rendererPipeline = rendererPipeline;
    }

    /**
     * Populates the rendering context with all necessary parameters required to render a specific animatable object.
     * This method initializes the rendering pipeline with data such as the model, buffer source, lighting, and other
     * associated properties for rendering the specified animatable object.
     *
     * @param animatable        The animatable object that is being rendered.
     * @param bakedModel        The pre-baked 3D model associated with the animatable object.
     * @param multiBufferSource The buffer source used for rendering vertex data.
     * @param packedLight       The packed light value for controlling light effects during rendering.
     * @param partialTick       The partial tick value for interpolating animations or movements.
     * @param poseStack         The pose stack used to manage rendering transformations.
     * @param renderType        The render type that determines how the object will be rendered, e.g., opaque,
     *                          translucent, etc.
     * @param vertexConsumer    The vertex consumer used for buffering vertex attributes during rendering.
     */
    public void populate(
        T animatable,
        AzBakedModel bakedModel,
        AzBufferSource multiBufferSource,
        int packedLight,
        float partialTick,
        PoseStack poseStack,
        RenderType renderType,
        VertexConsumer vertexConsumer
    ) {
        this.animatable = animatable;
        this.bakedModel = bakedModel;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
        this.packedOverlay = getPackedOverlay(animatable, 0, partialTick);
        this.partialTick = partialTick;
        this.poseStack = poseStack;
        this.vertexConsumer = vertexConsumer;
        this.renderColor = getRenderColor(animatable, partialTick, packedLight).argbInt();
        this.inModelPass = false;
        this.modelPassRenderType = null;
        this.modelPassStart = 0;
        this.modelPassEnd = 0;
        this.modelPassMirrorable = false;

        if (renderType == null) {
            var cfg = rendererPipeline.config();
            var texture = cfg.textureLocation(currentEntity, animatable);

            this.renderType = getDefaultRenderType(
                animatable,
                texture,
                multiBufferSource,
                partialTick,
                cfg.getRenderType(currentEntity, animatable),
                cfg.alpha(animatable)
            );
        } else {
            this.renderType = renderType;
        }

        if (this.vertexConsumer == null && this.renderType != null) {
            this.vertexConsumer = multiBufferSource.getBuffer(this.renderType);
        }
    }

    public abstract RenderType getDefaultRenderType(
        T animatable,
        Identifier texture,
        @Nullable AzBufferSource bufferSource,
        float partialTick,
        RenderType defaultRenderType,
        float alpha
    );

    /**
     * Gets a tint-applying color to render the given animatable with.<br>
     * Returns {@link Color#WHITE} by default
     */
    protected Color getRenderColor(T animatable, float partialTick, int packedLight) {
        return Color.WHITE;
    }

    /**
     * Gets a packed overlay coordinate pair for rendering.<br>
     * Mostly just used for the red tint when an entity is hurt, but can be used for other things like the
     * {@link net.minecraft.world.entity.monster.Creeper} white tint when exploding.
     */
    protected int getPackedOverlay(T animatable, float u, float partialTick) {
        return OverlayTexture.NO_OVERLAY;
    }

    public AzRendererPipeline<K, T> rendererPipeline() {
        return rendererPipeline;
    }

    public T animatable() {
        return animatable;
    }

    public void setCurrentEntity(Entity currentEntity) {
        this.currentEntity = currentEntity;
    }

    public @Nullable Entity currentEntity() {
        return currentEntity;
    }

    public AzBakedModel bakedModel() {
        return bakedModel;
    }

    public void setBakedModel(@Nullable AzBakedModel bakedModel) {
        this.bakedModel = bakedModel != null ? bakedModel : AzBakedModel.getDefault();
    }

    public boolean applyAnimationOnReRender() {
        return this.applyAnimationOnReRender;
    }

    public void setApplyAnimationOnReRender(boolean applyAnimationOnReRender) {
        this.applyAnimationOnReRender = applyAnimationOnReRender;
    }

    public AzBufferSource multiBufferSource() {
        return multiBufferSource;
    }

    public int packedLight() {
        return packedLight;
    }

    public void setPackedLight(int packedLight) {
        this.packedLight = packedLight;
    }

    public int packedOverlay() {
        return packedOverlay;
    }

    public void setPackedOverlay(int packedOverlay) {
        this.packedOverlay = packedOverlay;
    }

    public float partialTick() {
        return partialTick;
    }

    public PoseStack poseStack() {
        return poseStack;
    }

    public int renderColor() {
        return renderColor;
    }

    public void setRenderColor(int renderColor) {
        this.renderColor = renderColor;
    }

    public @Nullable RenderType renderType() {
        return renderType;
    }

    public void setRenderType(@Nullable RenderType renderType) {
        this.renderType = renderType;
    }

    public VertexConsumer vertexConsumer() {
        return vertexConsumer;
    }

    public void setVertexConsumer(VertexConsumer vertexConsumer) {
        this.vertexConsumer = vertexConsumer;
    }

    /**
     * Sets the texture override for the current rendering context. This can be used to replace the default texture
     * associated with the animatable object being rendered.
     *
     * @param textureOverride the {@link Identifier} of the texture to override; passing null will revert back to the
     *                        default texture
     */
    public void setTextureOverride(Identifier textureOverride) {
        this.textureOverride = textureOverride;
    }

    /**
     * Retrieves the texture override set for this rendering context, if any.
     *
     * @return the {@link Identifier} representing the texture override, or null if no override is set.
     */
    public Identifier getTextureOverride() {
        return textureOverride;
    }

    /**
     * Computes the dimensions of the specified texture and caches the result for future use. This method retrieves the
     * dimensions of the texture represented by the given {@code Identifier} and returns them as an {@code IntIntPair},
     * where the first value represents the width and the second value represents the height of the texture.
     *
     * @param texture the {@link Identifier} of the texture whose dimensions need to be computed
     * @return an {@link IntIntPair} containing the width and height of the texture
     */
    public IntIntPair computeTextureSize(Identifier texture) {
        return TEXTURE_DIMENSIONS_CACHE.computeIfAbsent(texture, RenderUtils::getTextureDimensions);
    }

    /**
     * Marks the start of the main model pass, recording where its vertices begin in the current render type's
     * recording. Called by {@link AzRendererPipeline#render}; see {@link #isModelPassMirrorable()}.
     */
    public void beginModelPass() {
        var source = multiBufferSource;
        inModelPass = true;
        modelPassRenderType = renderType;
        modelPassStart = source == null ? 0 : source.vertexCount(renderType);
        modelPassEnd = modelPassStart;
        modelPassTotalStart = source == null ? 0 : source.totalVertexCount();
        modelPassLayerVertices = 0;
        modelPassMirrorable = source != null && renderType != null;
    }

    /**
     * Marks the end of the main model pass. The pass stays mirrorable only if every vertex it produced, apart from
     * those written by per-bone layers, went into the main render type: a bone texture or render type override sends
     * vertices elsewhere, and a re-render would reproduce those differently.
     */
    public void endModelPass() {
        if (!inModelPass) {
            return;
        }

        inModelPass = false;

        if (!modelPassMirrorable) {
            return;
        }

        var source = multiBufferSource;
        modelPassEnd = source.vertexCount(modelPassRenderType);
        var modelVertices = source.totalVertexCount() - modelPassTotalStart - modelPassLayerVertices;

        if (modelVertices != modelPassEnd - modelPassStart) {
            modelPassMirrorable = false;
        }
    }

    /**
     * Accounts for vertices written by per-bone layers during the model pass. Writes into other render types (held
     * items, for example) are excluded from the pass; writes into the main render type can't be told apart from the
     * model, so they make the pass non-mirrorable.
     */
    public void recordBoneLayerWrites(int totalVertices, int modelRenderTypeVertices) {
        if (!inModelPass) {
            return;
        }

        if (modelRenderTypeVertices != 0) {
            modelPassMirrorable = false;
        }

        modelPassLayerVertices += totalVertices;
    }

    public boolean isInModelPass() {
        return inModelPass;
    }

    /**
     * Whether the last model pass for this render can be re-submitted under another render type with
     * {@link AzBufferSource#mirror} rather than re-rendered: it wrote at least one vertex, all into
     * {@link #modelPassRenderType()}, and nothing else wrote into that render type during the pass.
     */
    public boolean isModelPassMirrorable() {
        return modelPassMirrorable && !inModelPass && modelPassEnd > modelPassStart;
    }

    public @Nullable RenderType modelPassRenderType() {
        return modelPassRenderType;
    }

    public int modelPassStart() {
        return modelPassStart;
    }

    public int modelPassEnd() {
        return modelPassEnd;
    }
}
