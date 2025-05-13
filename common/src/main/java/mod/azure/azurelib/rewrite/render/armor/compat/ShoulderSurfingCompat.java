package mod.azure.azurelib.rewrite.render.armor.compat;

import com.github.exopandora.shouldersurfing.api.client.ShoulderSurfing;
import com.mojang.blaze3d.systems.RenderSystem;

public class ShoulderSurfingCompat {
	public static void setAlpha() {
		float alpha = ShoulderSurfing.getInstance().getCameraEntityRenderer().getCameraEntityAlpha();

		RenderSystem.setShaderColor(1, 1, 1, alpha);
	}
}
