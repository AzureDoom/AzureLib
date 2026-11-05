package mod.azure.azurelib.common.model;

import org.joml.Matrix4f;

import mod.azure.azurelib.common.util.client.RenderUtils;

/**
 * Bone transform math for mesh skinning, kept in step with {@link RenderUtils#prepMatrixForBone}.
 */
public final class AzSkinning {

    private AzSkinning() {}

    public static Matrix4f mulBoneLocal(Matrix4f matrix, AzBone bone) {
        return mulLocal(
            matrix,
            bone.getPosX(),
            bone.getPosY(),
            bone.getPosZ(),
            bone.getPivotX(),
            bone.getPivotY(),
            bone.getPivotZ(),
            bone.getRotX(),
            bone.getRotY(),
            bone.getRotZ(),
            bone.getScaleX(),
            bone.getScaleY(),
            bone.getScaleZ()
        );
    }

    public static Matrix4f mulLocal(
        Matrix4f matrix,
        float posX,
        float posY,
        float posZ,
        float pivotX,
        float pivotY,
        float pivotZ,
        float rotX,
        float rotY,
        float rotZ,
        float scaleX,
        float scaleY,
        float scaleZ
    ) {
        float px = pivotX / 16f;
        float py = pivotY / 16f;
        float pz = pivotZ / 16f;

        matrix.translate(-posX / 16f, posY / 16f, posZ / 16f);
        matrix.translate(px, py, pz);

        if (rotZ != 0f)
            matrix.rotateZ(rotZ);

        if (rotY != 0f)
            matrix.rotateY(rotY);

        if (rotX != 0f)
            matrix.rotateX(rotX);

        matrix.scale(scaleX, scaleY, scaleZ);

        return matrix.translate(-px, -py, -pz);
    }

    public static Matrix4f chainMatrix(AzBone[] chain, Matrix4f out) {
        out.identity();

        for (AzBone bone : chain) {
            mulBoneLocal(out, bone);
        }

        return out;
    }
}
