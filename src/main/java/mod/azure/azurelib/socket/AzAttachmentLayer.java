package mod.azure.azurelib.socket;

import java.util.*;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzBoneLocator;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.layer.AzRenderLayer;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.util.math.Vector3f;

/**
 * A render layer that draws {@link AzAttachment}s at {@link AzBoneSocket}s, so accessories, held items and props follow
 * animated bones without being part of the model.
 *
 * <pre>
 * {@code
 * addRenderLayer(
 *     new AzAttachmentLayer<UUID, MyEntity>()
 *         .attach(
 *             AzBoneSocket.bone("right_hand", "right_arm").offset(-1, -10, 0).build(),
 *             AzAttachment.item(MyEntity::getMainHandItem, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND)
 *         )
 *         .attach(
 *             AzBoneSocket.locator("lantern").build(),
 *             AzAttachment.block(entity -> Blocks.LANTERN.defaultBlockState())
 *         )
 * );
 * }
 * </pre>
 *
 * @param <K> the animatable's key type
 * @param <T> the animatable type
 */
public class AzAttachmentLayer<K, T> implements AzRenderLayer<K, T> {

    private final class Entry<T> {

        private final AzBoneSocket socket;

        private final AzAttachment<T> attachment;

        public Entry(AzBoneSocket socket, AzAttachment<T> attachment) {
            this.socket = socket;
            this.attachment = attachment;
        }

        public AzBoneSocket socket() {
            return this.socket;
        }

        public AzAttachment<T> attachment() {
            return this.attachment;
        }
    }

    private final Map<String, List<Entry<T>>> byBone = new HashMap<>();

    private final Map<String, List<Entry<T>>> byLocator = new HashMap<>();

    /** Renders {@code attachment} at {@code socket}. */
    public AzAttachmentLayer<K, T> attach(AzBoneSocket socket, AzAttachment<T> attachment) {
        Objects.requireNonNull(socket, "socket");
        Objects.requireNonNull(attachment, "attachment");
        Entry<T> entry = new Entry<>(socket, attachment);

        if (socket.locator() != null) {
            byLocator.computeIfAbsent(socket.locator(), ignored -> new ArrayList<>()).add(entry);
        } else {
            byBone.computeIfAbsent(socket.bone(), ignored -> new ArrayList<>()).add(entry);
        }

        return this;
    }

    @Override
    public void preRender(AzRendererPipelineContext<K, T> context) {}

    @Override
    public void render(AzRendererPipelineContext<K, T> context) {}

    @Override
    public void renderForBone(AzRendererPipelineContext<K, T> context, AzBone bone) {
        boolean rendered = false;
        List<Entry<T>> entries = byBone.get(bone.getName());

        if (entries != null) {
            for (Entry<T> entry : entries) {
                renderAt(context, bone, null, entry);
            }

            rendered = true;
        }

        if (!byLocator.isEmpty() && !bone.getLocators().isEmpty()) {
            for (AzBoneLocator locator : bone.getLocators().values()) {
                List<Entry<T>> locatorEntries = byLocator.get(locator.name());

                if (locatorEntries == null) {
                    continue;
                }

                for (Entry<T> entry : locatorEntries) {
                    renderAt(context, bone, locator, entry);
                }

                rendered = true;
            }
        }

        if (rendered) {
            // Attachments draw with their own buffers; hand the model its buffer back for the remaining bones.
            context.setVertexConsumer(context.multiBufferSource().getBuffer(context.renderType()));
        }
    }

    private void renderAt(
        AzRendererPipelineContext<K, T> context,
        AzBone bone,
        AzBoneLocator locator,
        Entry<T> entry
    ) {
        PoseStack poseStack = context.poseStack();
        AzBoneSocket socket = entry.socket();
        poseStack.pushPose();

        try {
            // renderForBone runs in the bone's child frame; move to its pivot, where bone-space offsets start.
            poseStack.translate(bone.getPivotX() / 16f, bone.getPivotY() / 16f, bone.getPivotZ() / 16f);

            // Offsets add up in bone space; rotations stack, the locator's first. AzBoneSockets resolves the same way.
            if (locator != null) {
                place(
                    poseStack,
                    locator.offsetX() + socket.offsetX(),
                    locator.offsetY() + socket.offsetY(),
                    locator.offsetZ() + socket.offsetZ()
                );
                rotate(poseStack, locator.rotationX(), locator.rotationY(), locator.rotationZ());
            } else {
                place(poseStack, socket.offsetX(), socket.offsetY(), socket.offsetZ());
            }

            rotate(poseStack, socket.rotationX(), socket.rotationY(), socket.rotationZ());
            entry.attachment().render(context, bone);
        } finally {
            poseStack.popPose();
        }
    }

    /** Moves by a Blockbench offset in pixels (X mirrored, as AzureLib mirrors it at load). */
    private static void place(PoseStack poseStack, float x, float y, float z) {
        if (x != 0 || y != 0 || z != 0) {
            poseStack.translate(-x / 16f, y / 16f, z / 16f);
        }
    }

    /** Applies a Blockbench rotation the way bone rotations are applied: Z, then Y, then X, with X and Y negated. */
    private static void rotate(PoseStack poseStack, float x, float y, float z) {
        if (z != 0) {
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(z));
        }

        if (y != 0) {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-y));
        }

        if (x != 0) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-x));
        }
    }
}
