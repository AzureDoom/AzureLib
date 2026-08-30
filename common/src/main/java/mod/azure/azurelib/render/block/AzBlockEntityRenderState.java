package mod.azure.azurelib.render.block;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.render.AzBufferSource;

/**
 * Render state carrying the geometry AzureLib baked for a single block entity during
 * {@link AzBlockEntityRenderer#extractRenderState}. See {@code AzEntityRenderState} for the rationale.
 */
public class AzBlockEntityRenderState extends BlockEntityRenderState {

    public @Nullable AzBufferSource geometry;
}
