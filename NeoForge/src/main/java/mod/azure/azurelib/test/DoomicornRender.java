package mod.azure.azurelib.test;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.model.DefaultedItemGeoModel;
import mod.azure.azurelib.renderer.GeoArmorRenderer;
import net.minecraft.resources.ResourceLocation;

public class DoomicornRender extends GeoArmorRenderer<DoomicornDoomArmor> {
	public DoomicornRender() {
		super(new DefaultedItemGeoModel(new ResourceLocation(AzureLib.MOD_ID,"doomicorn_armor")));
	}

	@Override
	public GeoBone getLeftBootBone() {
		return model.getBone("armorRightBoot").orElse(null);
	}

	@Override
	public GeoBone getLeftLegBone() {
		return model.getBone("armorRightLeg").orElse(null);
	}

	@Override
	public GeoBone getRightBootBone() {
		return model.getBone("armorLeftBoot").orElse(null);
	}

	@Override
	public GeoBone getRightLegBone() {
		return model.getBone("armorLeftLeg").orElse(null);
	}
}