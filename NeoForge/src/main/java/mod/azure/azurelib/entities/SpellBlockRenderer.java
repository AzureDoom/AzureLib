package mod.azure.azurelib.entities;

import mod.azure.azurelib.renderer.GeoBlockRenderer;

public class SpellBlockRenderer extends GeoBlockRenderer<TimerSpellTurrentEntity> {
	public SpellBlockRenderer() {
		super(new SpellBlockModel());
	}
}