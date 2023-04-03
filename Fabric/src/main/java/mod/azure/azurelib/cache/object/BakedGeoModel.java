package mod.azure.azurelib.cache.object;

import java.util.List;
import java.util.Optional;

import mod.azure.azurelib.core.animatable.model.CoreBakedGeoModel;
import mod.azure.azurelib.core.animatable.model.CoreGeoBone;
import mod.azure.azurelib.loading.json.raw.ModelProperties;

/**
 * Baked model object for AzureLib models.
 */
public class BakedGeoModel implements CoreBakedGeoModel {
	public List<GeoBone> topLevelBones;
	public ModelProperties properties;

	public BakedGeoModel(List<GeoBone> topLevelBones, ModelProperties properties) {
		this.topLevelBones = topLevelBones;
		this.properties = properties;
	}

	public List<GeoBone> topLevelBones() {
		return topLevelBones;
	}

	public ModelProperties properties() {
		return properties;
	}

	/**
	 * Gets the list of top-level bones for this model. Identical to calling {@link BakedGeoModel#topLevelBones()}
	 */
	@Override
	public List<? extends CoreGeoBone> getBones() {
		return this.topLevelBones;
	}

	/**
	 * Gets a bone from this model by name.<br>
	 * Generally not a very efficient method, should be avoided where possible.
	 * 
	 * @param name The name of the bone
	 * @return An {@link Optional} containing the {@link GeoBone} if one matches, otherwise an empty Optional
	 */
	public Optional<GeoBone> getBone(String name) {
		for (GeoBone bone : this.topLevelBones) {
			CoreGeoBone childBone = searchForChildBone(bone, name);

			if (childBone != null)
				return Optional.of((GeoBone) childBone);
		}

		return Optional.empty();
	}
}
