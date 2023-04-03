package mod.azure.azurelib.loading.object;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.cache.object.GeoQuad;
import mod.azure.azurelib.cache.object.GeoVertex;
import mod.azure.azurelib.loading.json.raw.Bone;
import mod.azure.azurelib.loading.json.raw.Cube;
import mod.azure.azurelib.loading.json.raw.FaceUV;
import mod.azure.azurelib.loading.json.raw.ModelProperties;
import mod.azure.azurelib.loading.json.raw.UVUnion;
import mod.azure.azurelib.util.AzureLibUtil;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Base interface for a factory of {@link BakedGeoModel} objects.
 * Handled by default by AzureLib, but custom implementations may be added by other mods for special needs
 */
public interface BakedModelFactory {
	final Map<String, BakedModelFactory> FACTORIES = new Object2ObjectOpenHashMap<>(1);
	final BakedModelFactory DEFAULT_FACTORY = new Builtin();

	/**
	 * Construct the output model from the given {@link GeometryTree}.<br>
	 */
	BakedGeoModel constructGeoModel(GeometryTree geometryTree);

	/**
	 * Construct a {@link GeoBone} from the relevant raw input data
	 * @param boneStructure The {@code BoneStructure} comprising the structure of the bone and its children
	 * @param properties The loaded properties for the model
	 * @param parent The parent bone for this bone, or null if a top-level bone
	 */
	GeoBone constructBone(BoneStructure boneStructure, ModelProperties properties, @Nullable GeoBone parent);

	/**
	 * Construct a {@link GeoCube} from the relevant raw input data
	 * @param cube The raw {@code Cube} comprising the structure and properties of the cube
	 * @param properties The loaded properties for the model
	 * @param bone The bone this cube belongs to
	 */
	GeoCube constructCube(Cube cube, ModelProperties properties, GeoBone bone);

	/**
	 * Builtin method to construct the quad list from the various vertices and related data, to make it easier.<br>
	 * Vertices have already been mirrored here if {@code mirror} is true
	 */
	default GeoQuad[] buildQuads(UVUnion uvUnion, VertexSet vertices, Cube cube, float textureWidth, float textureHeight, boolean mirror) {
		GeoQuad[] quads = new GeoQuad[6];

		quads[0] = buildQuad(vertices, cube, uvUnion, textureWidth, textureHeight, mirror, Direction.WEST);
		quads[1] = buildQuad(vertices, cube, uvUnion, textureWidth, textureHeight, mirror, Direction.EAST);
		quads[2] = buildQuad(vertices, cube, uvUnion, textureWidth, textureHeight, mirror, Direction.NORTH);
		quads[3] = buildQuad(vertices, cube, uvUnion, textureWidth, textureHeight, mirror, Direction.SOUTH);
		quads[4] = buildQuad(vertices, cube, uvUnion, textureWidth, textureHeight, mirror, Direction.UP);
		quads[5] = buildQuad(vertices, cube, uvUnion, textureWidth, textureHeight, mirror, Direction.DOWN);

		return quads;
	}

	/**
	 * Build an individual quad
	 */
	default GeoQuad buildQuad(VertexSet vertices, Cube cube, UVUnion uvUnion, float textureWidth, float textureHeight, boolean mirror, Direction direction) {
		if (!uvUnion.isBoxUV()) {
			FaceUV faceUV = uvUnion.faceUV().fromDirection(direction);

			if (faceUV == null)
				return null;

			return GeoQuad.build(vertices.verticesForQuad(direction, false, mirror || cube.mirror() == Boolean.TRUE), faceUV.uv(), faceUV.uvSize(),
					textureWidth, textureHeight, mirror, direction);
		}

		double[] uv = cube.uv().boxUVCoords();
		double[] uvSize = cube.size();
		Vector3d uvSizeVec = new Vector3d(Math.floor(uvSize[0]), Math.floor(uvSize[1]), Math.floor(uvSize[2]));
		double[][] uvData = new double[][] {new double[] {0}, new double[] {0}};
		switch(direction) {
			case WEST:
				uvData = new double[][] {
					new double[] {uv[0] + uvSizeVec.z + uvSizeVec.x, uv[1] + uvSizeVec.z},
					new double[] {uvSizeVec.z, uvSizeVec.y}
				};
				break;
			case EAST: 
				uvData = new double[][] {
					new double[] { uv[0], uv[1] + uvSizeVec.z },
					new double[] { uvSizeVec.z, uvSizeVec.y }
				};
				break;
			case NORTH: 
				uvData = new double[][] {
					new double[] {uv[0] + uvSizeVec.z, uv[1] + uvSizeVec.z},
					new double[] {uvSizeVec.x, uvSizeVec.y}
				};
				break;
			case SOUTH: 
				uvData = new double[][] {
					new double[] {uv[0] + uvSizeVec.z + uvSizeVec.x + uvSizeVec.z, uv[1] + uvSizeVec.z},
					new double[] {uvSizeVec.x, uvSizeVec.y }
				};
				break;
			case UP: 
				uvData = new double[][] {
					new double[] {uv[0] + uvSizeVec.z, uv[1]},
					new double[] {uvSizeVec.x, uvSizeVec.z}
				};
				break;
			case DOWN: 
				uvData = new double[][] {
					new double[] {uv[0] + uvSizeVec.z + uvSizeVec.x, uv[1] + uvSizeVec.z},
					new double[] {uvSizeVec.x, -uvSizeVec.z}
				};
				break;
		};

		return GeoQuad.build(vertices.verticesForQuad(direction, true, mirror || cube.mirror() == Boolean.TRUE), uvData[0], uvData[1], textureWidth, textureHeight, mirror, direction);
	}

	static BakedModelFactory getForNamespace(String namespace) {
		return FACTORIES.getOrDefault(namespace, DEFAULT_FACTORY);
	}

	/**
	 * Register a custom {@link BakedModelFactory} to handle loading models in a custom way.<br>
	 * <b><u>MUST be called during mod construct</u></b><br>
	 * It is recommended you don't call this directly, and instead call it via {@link AzureLibUtil#addCustomBakedModelFactory}
	 * @param namespace The namespace (modid) to register the factory for
	 * @param factory The factory responsible for model loading under the given namespace
	 */
	static void register(String namespace, BakedModelFactory factory) {
		FACTORIES.put(namespace, factory);
	}

	final class Builtin implements BakedModelFactory {
		@Override
		public BakedGeoModel constructGeoModel(GeometryTree geometryTree) {
			List<GeoBone> bones = new ObjectArrayList<>();

			for (BoneStructure boneStructure : geometryTree.topLevelBones().values()) {
				bones.add(constructBone(boneStructure, geometryTree.properties(), null));
			}

			return new BakedGeoModel(bones, geometryTree.properties());
		}

		@Override
		public GeoBone constructBone(BoneStructure boneStructure, ModelProperties properties, GeoBone parent) {
			Bone bone = boneStructure.self();
			GeoBone newBone = new GeoBone(parent, bone.name(), bone.mirror(), bone.inflate(), bone.neverRender(), bone.reset());
			Vector3d rotation = RenderUtils.arrayToVec(bone.rotation());
			Vector3d pivot = RenderUtils.arrayToVec(bone.pivot());

			newBone.updateRotation((float)Math.toRadians(-rotation.x), (float)Math.toRadians(-rotation.y), (float)Math.toRadians(rotation.z));
			newBone.updatePivot((float)-pivot.x, (float)pivot.y, (float)pivot.z);

			for (Cube cube : bone.cubes()) {
				newBone.getCubes().add(constructCube(cube, properties, newBone));
			}

			for (BoneStructure child : boneStructure.children().values()) {
				newBone.getChildBones().add(constructBone(child, properties, newBone));
			}

			return newBone;
		}

		@Override
		public GeoCube constructCube(Cube cube, ModelProperties properties, GeoBone bone) {
			boolean mirror = cube.mirror() == Boolean.TRUE;
			double inflate = cube.inflate() != null ? cube.inflate() / 16f : (bone.getInflate() == null ? 0 : bone.getInflate() / 16f);
			Vector3d size = RenderUtils.arrayToVec(cube.size());
			Vector3d origin = RenderUtils.arrayToVec(cube.origin());
			Vector3d rotation = RenderUtils.arrayToVec(cube.rotation());
			Vector3d pivot = RenderUtils.arrayToVec(cube.pivot());
			origin = new Vector3d(-(origin.x + size.x) / 16d, origin.y / 16d, origin.z / 16d);
			Vector3d vertexSize = size.multiply(1 / 16d, 1 / 16d, 1 / 16d);

			pivot = pivot.multiply(-1, 1, 1);
			rotation = new Vector3d(Math.toRadians(-rotation.x), Math.toRadians(-rotation.y), Math.toRadians(rotation.z));
			GeoQuad[] quads = buildQuads(cube.uv(), new VertexSet(origin, vertexSize, inflate), cube, (float)properties.textureWidth(), (float)properties.textureHeight(), mirror);

			return new GeoCube(quads, pivot, rotation, size, inflate, mirror);
		}
	}

	/**
	 * Holder class to make it easier to store and refer to vertices for a given cube
	 */
	class VertexSet {
		
		protected final GeoVertex bottomLeftBack;
		protected final GeoVertex bottomRightBack;
		protected final GeoVertex topLeftBack;
		protected final GeoVertex topRightBack;
		protected final	GeoVertex topLeftFront;
		protected final GeoVertex topRightFront;
		protected final GeoVertex bottomLeftFront;
		protected final GeoVertex bottomRightFront;
		
		public VertexSet(GeoVertex bottomLeftBack, GeoVertex bottomRightBack, GeoVertex topLeftBack, GeoVertex topRightBack,
				GeoVertex topLeftFront, GeoVertex topRightFront, GeoVertex bottomLeftFront, GeoVertex bottomRightFront) {
			this.topRightFront = topRightFront;
			this.bottomLeftBack = bottomLeftBack;
			this.bottomRightBack = bottomRightBack;
			this.topLeftBack = topLeftBack;
			this.topRightBack = topRightBack;
			this.topLeftFront = topLeftFront;
			this.bottomLeftFront = bottomLeftFront;
			this.bottomRightFront = bottomRightFront;
			
		}
		
		public VertexSet(Vector3d origin, Vector3d vertexSize, double inflation) {
			this(
					new GeoVertex(origin.x - inflation, origin.y - inflation, origin.z - inflation),
					new GeoVertex(origin.x - inflation, origin.y - inflation, origin.z + vertexSize.z + inflation),
					new GeoVertex(origin.x - inflation, origin.y + vertexSize.y + inflation, origin.z - inflation),
					new GeoVertex(origin.x - inflation, origin.y + vertexSize.y + inflation, origin.z + vertexSize.z + inflation),
					new GeoVertex(origin.x + vertexSize.x + inflation, origin.y + vertexSize.y + inflation, origin.z - inflation),
					new GeoVertex(origin.x + vertexSize.x + inflation, origin.y + vertexSize.y + inflation, origin.z + vertexSize.z + inflation),
					new GeoVertex(origin.x + vertexSize.x + inflation, origin.y - inflation, origin.z - inflation),
					new GeoVertex(origin.x + vertexSize.x + inflation, origin.y - inflation, origin.z + vertexSize.z + inflation));
		}

		/**
		 * Returns the normal vertex array for a west-facing quad
		 */
		public GeoVertex[] quadWest() {
			return new GeoVertex[] {this.topRightBack, this.topLeftBack, this.bottomLeftBack, this.bottomRightBack};
		}

		/**
		 * Returns the normal vertex array for an east-facing quad
		 */
		public GeoVertex[] quadEast() {
			return new GeoVertex[] {this.topLeftFront, this.topRightFront, this.bottomRightFront, this.bottomLeftFront};
		}

		/**
		 * Returns the normal vertex array for a north-facing quad
		 */
		public GeoVertex[] quadNorth() {
			return new GeoVertex[] {this.topLeftBack, this.topLeftFront, this.bottomLeftFront, this.bottomLeftBack};
		}

		/**
		 * Returns the normal vertex array for a south-facing quad
		 */
		public GeoVertex[] quadSouth() {
			return new GeoVertex[] {this.topRightFront, this.topRightBack, this.bottomRightBack, this.bottomRightFront};
		}

		/**
		 * Returns the normal vertex array for a top-facing quad
		 */
		public GeoVertex[] quadUp() {
			return new GeoVertex[] {this.topRightBack, this.topRightFront, this.topLeftFront, this.topLeftBack};
		}

		/**
		 * Returns the normal vertex array for a bottom-facing quad
		 */
		public GeoVertex[] quadDown() {
			return new GeoVertex[] {this.bottomLeftBack, this.bottomLeftFront, this.bottomRightFront, this.bottomRightBack};
		}

		/**
		 * Return the vertex array relevant to the quad being built, taking into account mirroring and quad type
		 */
		public GeoVertex[] verticesForQuad(Direction direction, boolean boxUv, boolean mirror) {
			switch (direction) {
				case WEST:
					return mirror ? quadEast() : quadWest();
				case EAST: 
					return mirror ? quadWest() : quadEast();
				case NORTH:
					return quadNorth();
				case SOUTH:
					return quadSouth();
				case UP:
					return mirror && !boxUv ? quadDown() : quadUp();
				case DOWN:
					return mirror && !boxUv ? quadUp() : quadDown();
			};
			return this.quadUp();
		}

		public GeoVertex bottomLeftBack() {
			return bottomLeftBack;
		}

		public GeoVertex bottomRightBack() {
			return bottomRightBack;
		}

		public GeoVertex topLeftBack() {
			return topLeftBack;
		}

		public GeoVertex topRightBack() {
			return topRightBack;
		}

		public GeoVertex topLeftFront() {
			return topLeftFront;
		}

		public GeoVertex topRightFront() {
			return topRightFront;
		}

		public GeoVertex bottomLeftFront() {
			return bottomLeftFront;
		}

		public GeoVertex bottomRightFront() {
			return bottomRightFront;
		}
	}
}
