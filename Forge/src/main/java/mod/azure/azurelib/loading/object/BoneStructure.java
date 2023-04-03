package mod.azure.azurelib.loading.object;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mod.azure.azurelib.loading.json.raw.Bone;

import java.util.Map;

/**
 * Container class for holding a {@link Bone} structure. Used at startup in deserialization
 */
public class BoneStructure {
	
	protected final Bone self;
	protected final Map<String, BoneStructure> children;
	
	public BoneStructure(Bone self, Map<String, BoneStructure> children) {
		this.self = self;
		this.children = children;
	}
	
	public BoneStructure(Bone self) {
		this(self, new Object2ObjectOpenHashMap<>());
	}
	
	public Bone self() {
		return this.self;
	}
	
	public Map<String, BoneStructure> children() {
		return this.children;
	}
}
