package mod.azure.azurelib.model.factory.impl;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.phys.Vec3;

import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.loading.json.raw.Cube;
import mod.azure.azurelib.loading.json.raw.ModelProperties;
import mod.azure.azurelib.loading.object.BoneStructure;
import mod.azure.azurelib.loading.object.GeometryTree;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzBoneMetadata;
import mod.azure.azurelib.model.factory.AzBakedModelFactory;
import mod.azure.azurelib.model.factory.primitive.VertexSet;
import mod.azure.azurelib.util.RenderUtils;

/**
 * A concrete implementation of the {@link AzBakedModelFactory} that constructs baked models, bones, and cubes from raw
 * geometry data. It is tailored to create and configure the model and its associated components in a hierarchical
 * manner, based on the provided structure and properties.
 */
public final class AzBuiltinBakedModelFactory extends AzBakedModelFactory {

    @Override
    public AzBakedModel constructGeoModel(GeometryTree geometryTree) {
        ObjectArrayList<AzBone> bones = new ObjectArrayList<>();

        for (BoneStructure boneStructure : geometryTree.topLevelBones().values()) {
            bones.add(constructBone(boneStructure, geometryTree.properties(), null));
        }

        return new AzBakedModel(bones);
    }

    @Override
    public AzBone constructBone(BoneStructure boneStructure, ModelProperties properties, AzBone parent) {
        mod.azure.azurelib.loading.json.raw.Bone bone = boneStructure.self();
        AzBoneMetadata boneMetadata = new AzBoneMetadata(bone, parent);
        AzBone newBone = new AzBone(boneMetadata);
        Vec3 rotation = RenderUtils.arrayToVec(bone.rotation());
        Vec3 pivot = RenderUtils.arrayToVec(bone.pivot());

        newBone.updateRotation(
            (float) Math.toRadians(-rotation.x),
            (float) Math.toRadians(-rotation.y),
            (float) Math.toRadians(rotation.z)
        );
        newBone.updatePivot((float) -pivot.x, (float) pivot.y, (float) pivot.z);

        for (Cube cube : bone.cubes()) {
            newBone.getCubes().add(constructCube(cube, properties, newBone));
        }

        // TODO: Avoid recursive calls here.
        for (BoneStructure child : boneStructure.children().values()) {
            newBone.getChildBones().add(constructBone(child, properties, newBone));
        }

        return newBone;
    }

    @Override
    public GeoCube constructCube(Cube cube, ModelProperties properties, AzBone bone) {
        boolean mirror = cube.mirror() == Boolean.TRUE;
        double inflate = cube.inflate() != null
            ? cube.inflate() / 16f
            : (bone.getInflate() == null ? 0 : bone.getInflate() / 16f);
        Vec3 size = RenderUtils.arrayToVec(cube.size());
        Vec3 origin = RenderUtils.arrayToVec(cube.origin());
        Vec3 rotation = RenderUtils.arrayToVec(cube.rotation());
        Vec3 pivot = RenderUtils.arrayToVec(cube.pivot());
        origin = new Vec3(-(origin.x + size.x) / 16d, origin.y / 16d, origin.z / 16d);
        Vec3 vertexSize = size.multiply(1 / 16d, 1 / 16d, 1 / 16d);

        pivot = pivot.multiply(-1, 1, 1);
        rotation = new Vec3(Math.toRadians(-rotation.x), Math.toRadians(-rotation.y), Math.toRadians(rotation.z));
        mod.azure.azurelib.cache.object.GeoQuad[] quads = buildQuads(
            cube.uv(),
            new VertexSet(origin, vertexSize, inflate),
            cube,
            (float) properties.textureWidth(),
            (float) properties.textureHeight(),
            mirror
        );

        return new GeoCube(quads, pivot, rotation, size, inflate, mirror);
    }
}
