package mod.azure.azurelib.render.entity;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.render.AzBufferSource;

/**
 * Render state carrying the geometry AzureLib baked for a single entity during
 * {@link AzEntityRenderer#extractRenderState}.
 * <p>
 * 26.2 splits entity rendering into an extract phase (allowed to read the entity) and a submit phase (not allowed to).
 * AzureLib drives its animation and procedural passes off the live entity, so the whole pipeline runs during extract
 * against an identity pose and records its vertices into {@link #geometry}; submit only replays that recording with the
 * camera-relative pose the engine hands it.
 */
public class AzEntityRenderState extends EntityRenderState {

    public @Nullable AzBufferSource geometry;
}
