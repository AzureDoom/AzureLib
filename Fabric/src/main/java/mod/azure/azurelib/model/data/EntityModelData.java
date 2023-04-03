package mod.azure.azurelib.model.data;

/**
 * Container class for various pieces of data relating to a model's current state.
 */

public class EntityModelData {
	public boolean isSitting;
	public boolean isChild;
	public float netHeadYaw;
	public float headPitch;

	public EntityModelData(boolean shouldSit, boolean isChild, float netHeadYaw, float headPitch) {
		this.isSitting = shouldSit;
		this.isChild = isChild;
		this.netHeadYaw = netHeadYaw;
		this.headPitch = headPitch;
	}

	public boolean isSitting() {
		return isSitting;
	}

	public boolean isChild() {
		return isChild;
	}

	public float netHeadYaw() {
		return netHeadYaw;
	}

	public float headPitch() {
		return headPitch;
	}
}