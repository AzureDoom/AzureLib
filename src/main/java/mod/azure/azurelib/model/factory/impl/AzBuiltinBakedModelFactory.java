package mod.azure.azurelib.model.factory.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.cache.object.GeoQuad;
import mod.azure.azurelib.loading.json.raw.Bone;
import mod.azure.azurelib.loading.json.raw.Cube;
import mod.azure.azurelib.loading.json.raw.LocatorValue;
import mod.azure.azurelib.loading.json.raw.ModelProperties;
import mod.azure.azurelib.loading.object.BoneStructure;
import mod.azure.azurelib.loading.object.GeometryTree;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzBoneLocator;
import mod.azure.azurelib.model.AzBoneMetadata;
import mod.azure.azurelib.model.factory.AzBakedModelFactory;
import mod.azure.azurelib.model.factory.primitive.VertexSet;
import mod.azure.azurelib.util.client.RenderUtils;
import mod.azure.azurelib.util.math.Vec3;

/**
 * A concrete implementation of the {@link AzBakedModelFactory} that constructs baked models, bones, and cubes from raw
 * geometry data. It is tailored to create and configure the model and its associated components in a hierarchical
 * manner, based on the provided structure and properties.
 */
public final class AzBuiltinBakedModelFactory extends AzBakedModelFactory {

    @Override
    public AzBakedModel constructGeoModel(GeometryTree geometryTree) {
        ArrayList<AzBone> bones = new ArrayList<AzBone>();

        for (BoneStructure boneStructure : geometryTree.topLevelBones().values()) {
            bones.add(constructBone(boneStructure, geometryTree.properties(), null));
        }

        return new AzBakedModel(bones);
    }

    @Override
    public AzBone constructBone(BoneStructure boneStructure, ModelProperties properties, AzBone parent) {
        Bone bone = boneStructure.self();
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
        newBone.setLocators(bakeLocators(bone, pivot));

        for (Cube cube : bone.cubes()) {
            newBone.getCubes().add(constructCube(cube, properties, newBone));
        }

        // TODO: Avoid recursive calls here.
        for (BoneStructure child : boneStructure.children().values()) {
            newBone.getChildBones().add(constructBone(child, properties, newBone));
        }

        return newBone;
    }

    /** Bakes a bone's locators relative to its pivot, in Blockbench pixels and degrees. */
    private Map<String, AzBoneLocator> bakeLocators(Bone bone, Vec3 pivot) {
        Map<String, LocatorValue> locators = bone.locators();

        if (locators == null || locators.isEmpty()) {
            return Collections.emptyMap();
        }

        HashMap<String, AzBoneLocator> baked = new HashMap<>();

        for (Map.Entry<String, LocatorValue> entry : locators.entrySet()) {
            LocatorValue value = entry.getValue();
            double[] position;
            double[] rotation = null;

            if (value.locatorClass() != null) {
                position = value.locatorClass().offset();
                rotation = value.locatorClass().rotation();
            } else {
                position = value.values();
            }

            if (position == null || position.length < 3) {
                continue;
            }

            boolean hasRotation = rotation != null && rotation.length >= 3;
            baked.put(
                entry.getKey(),
                new AzBoneLocator(
                    entry.getKey(),
                    (float) (position[0] - pivot.x),
                    (float) (position[1] - pivot.y),
                    (float) (position[2] - pivot.z),
                    hasRotation ? (float) rotation[0] : 0,
                    hasRotation ? (float) rotation[1] : 0,
                    hasRotation ? (float) rotation[2] : 0
                )
            );
        }

        return baked;
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
        GeoQuad[] quads = buildQuads(
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
