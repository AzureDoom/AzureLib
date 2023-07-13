package mod.azure.azurelib.entities;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.model.DefaultedBlockGeoModel;
import net.minecraft.resources.ResourceLocation;

public class SpellBlockModel extends DefaultedBlockGeoModel<TimerSpellTurrentEntity> {

	public SpellBlockModel() {
		super(new ResourceLocation(AzureLib.MOD_ID, "timer_spell_turret"));
	}
}
