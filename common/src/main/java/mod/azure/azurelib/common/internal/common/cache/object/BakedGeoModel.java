package mod.azure.azurelib.common.internal.common.cache.object;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import mod.azure.azurelib.core.animatable.model.CoreBakedGeoModel;
import mod.azure.azurelib.core.animatable.model.CoreGeoBone;

/**
 * Baked model object for AzureLib models.
 */
public class BakedGeoModel implements CoreBakedGeoModel {

	public final Map<String, GeoBone> bonesByName;

	public final List<GeoBone> topLevelBones;

	public BakedGeoModel(List<GeoBone> topLevelBones) {
		this.bonesByName = new HashMap<>();
		this.topLevelBones = topLevelBones;
		mapBonesByName(topLevelBones);
	}

	private void mapBonesByName(List<GeoBone> geoBones) {
		geoBones.forEach(geoBone -> {
			bonesByName.put(geoBone.getName(), geoBone);
			mapBonesByName(geoBone.getChildBones());
		});
	}

	/**
	 * Gets the list of top-level bones for this model.
	 * Identical to calling {@link BakedGeoModel#getTopLevelBones()}
	 */
	@Override
	public List<? extends CoreGeoBone> getBones() {
		return this.topLevelBones;
	}

	/**
	 * Gets a bone from this model by name.<br>
	 * @param name The name of the bone
	 * @return An {@link Optional} containing the {@link GeoBone} if one matches, otherwise an empty Optional
	 */
	public Optional<GeoBone> getBone(String name) {
		return Optional.ofNullable(bonesByName.get(name));
	}

	public List<GeoBone> getTopLevelBones() {
		return topLevelBones;
	}
}
