package mod.azure.azurelib.model;

/**
 * Bone transform math for mesh skinning, kept in step with
 * {@link mod.azure.azurelib.util.client.RenderUtils#prepMatrixForBone}.
 * <p>
 * Matrices are {@code float[16]} in column-major order ({@code m[column * 4 + row]}), so this works the same on every
 * branch whatever math classes the game version provides. Every method writes into arrays the caller owns and
 * allocates nothing.
 * </p>
 */
public final class AzSkinning {

    private AzSkinning() {}

    /** Sets {@code out} to the identity matrix. */
    public static float[] identity(float[] out) {
        for (int i = 0; i < 16; i++) {
            out[i] = (i % 5 == 0) ? 1f : 0f;
        }

        return out;
    }

    /** Copies {@code source} into {@code out}. */
    public static float[] set(float[] out, float[] source) {
        System.arraycopy(source, 0, out, 0, 16);
        return out;
    }

    /** Multiplies {@code matrix} by the bone's current local transform, matching {@code prepMatrixForBone}. */
    public static float[] mulBoneLocal(float[] matrix, AzBone bone) {
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

    /**
     * Multiplies {@code matrix} by {@code T(position) * T(pivot) * Rz * Ry * Rx * S * T(-pivot)}. Position and pivot are
     * in AzBone units (pixels, X as stored on {@link AzBone}), rotation in radians; the result is in model units
     * (pixels / 16).
     */
    public static float[] mulLocal(
        float[] matrix,
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

        translate(matrix, -posX / 16f, posY / 16f, posZ / 16f);
        translate(matrix, px, py, pz);

        if (rotZ != 0f)
            rotateZ(matrix, rotZ);

        if (rotY != 0f)
            rotateY(matrix, rotY);

        if (rotX != 0f)
            rotateX(matrix, rotX);

        scale(matrix, scaleX, scaleY, scaleZ);
        return translate(matrix, -px, -py, -pz);
    }

    /** Writes the current model-space transform of the last bone in {@code chain} into {@code out}. */
    public static float[] chainMatrix(AzBone[] chain, float[] out) {
        identity(out);

        for (AzBone bone : chain) {
            mulBoneLocal(out, bone);
        }

        return out;
    }

    /** {@code m = m * T(x, y, z)}. */
    public static float[] translate(float[] m, float x, float y, float z) {
        m[12] += m[0] * x + m[4] * y + m[8] * z;
        m[13] += m[1] * x + m[5] * y + m[9] * z;
        m[14] += m[2] * x + m[6] * y + m[10] * z;
        m[15] += m[3] * x + m[7] * y + m[11] * z;
        return m;
    }

    /** {@code m = m * S(x, y, z)}. */
    public static float[] scale(float[] m, float x, float y, float z) {
        for (int r = 0; r < 4; r++) {
            m[r] *= x;
            m[4 + r] *= y;
            m[8 + r] *= z;
        }

        return m;
    }

    /** {@code m = m * Rx(angle)}. */
    public static float[] rotateX(float[] m, float angle) {
        float c = (float) Math.cos(angle), s = (float) Math.sin(angle);

        for (int r = 0; r < 4; r++) {
            float c1 = m[4 + r], c2 = m[8 + r];
            m[4 + r] = c * c1 + s * c2;
            m[8 + r] = -s * c1 + c * c2;
        }

        return m;
    }

    /** {@code m = m * Ry(angle)}. */
    public static float[] rotateY(float[] m, float angle) {
        float c = (float) Math.cos(angle), s = (float) Math.sin(angle);

        for (int r = 0; r < 4; r++) {
            float c0 = m[r], c2 = m[8 + r];
            m[r] = c * c0 - s * c2;
            m[8 + r] = s * c0 + c * c2;
        }

        return m;
    }

    /** {@code m = m * Rz(angle)}. */
    public static float[] rotateZ(float[] m, float angle) {
        float c = (float) Math.cos(angle), s = (float) Math.sin(angle);

        for (int r = 0; r < 4; r++) {
            float c0 = m[r], c1 = m[4 + r];
            m[r] = c * c0 + s * c1;
            m[4 + r] = -s * c0 + c * c1;
        }

        return m;
    }

    /** {@code out = a * b}. {@code out} may be {@code a} or {@code b}; {@code scratch} must be a different array. */
    public static float[] mul(float[] a, float[] b, float[] out, float[] scratch) {
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                scratch[col * 4 + row] = a[row] * b[col * 4]
                    + a[4 + row] * b[col * 4 + 1]
                    + a[8 + row] * b[col * 4 + 2]
                    + a[12 + row] * b[col * 4 + 3];
            }
        }

        System.arraycopy(scratch, 0, out, 0, 16);
        return out;
    }

    /**
     * Inverts {@code m} into {@code out} ({@code out} may be {@code m}). A singular matrix leaves {@code out} as the
     * identity, so a degenerate bone can't poison every vertex it influences.
     *
     * @return whether the matrix was invertible
     */
    public static boolean invert(float[] m, float[] out) {
        float a00 = m[0], a01 = m[1], a02 = m[2], a03 = m[3];
        float a10 = m[4], a11 = m[5], a12 = m[6], a13 = m[7];
        float a20 = m[8], a21 = m[9], a22 = m[10], a23 = m[11];
        float a30 = m[12], a31 = m[13], a32 = m[14], a33 = m[15];

        float b00 = a00 * a11 - a01 * a10, b01 = a00 * a12 - a02 * a10, b02 = a00 * a13 - a03 * a10;
        float b03 = a01 * a12 - a02 * a11, b04 = a01 * a13 - a03 * a11, b05 = a02 * a13 - a03 * a12;
        float b06 = a20 * a31 - a21 * a30, b07 = a20 * a32 - a22 * a30, b08 = a20 * a33 - a23 * a30;
        float b09 = a21 * a32 - a22 * a31, b10 = a21 * a33 - a23 * a31, b11 = a22 * a33 - a23 * a32;

        float det = b00 * b11 - b01 * b10 + b02 * b09 + b03 * b08 - b04 * b07 + b05 * b06;

        if (det == 0f || Float.isNaN(det) || Float.isInfinite(det)) {
            identity(out);
            return false;
        }

        float inv = 1f / det;
        out[0] = (a11 * b11 - a12 * b10 + a13 * b09) * inv;
        out[1] = (a02 * b10 - a01 * b11 - a03 * b09) * inv;
        out[2] = (a31 * b05 - a32 * b04 + a33 * b03) * inv;
        out[3] = (a22 * b04 - a21 * b05 - a23 * b03) * inv;
        out[4] = (a12 * b08 - a10 * b11 - a13 * b07) * inv;
        out[5] = (a00 * b11 - a02 * b08 + a03 * b07) * inv;
        out[6] = (a32 * b02 - a30 * b05 - a33 * b01) * inv;
        out[7] = (a20 * b05 - a22 * b02 + a23 * b01) * inv;
        out[8] = (a10 * b10 - a11 * b08 + a13 * b06) * inv;
        out[9] = (a01 * b08 - a00 * b10 - a03 * b06) * inv;
        out[10] = (a30 * b04 - a31 * b02 + a33 * b00) * inv;
        out[11] = (a21 * b02 - a20 * b04 - a23 * b00) * inv;
        out[12] = (a11 * b07 - a10 * b09 - a12 * b06) * inv;
        out[13] = (a00 * b09 - a01 * b07 + a02 * b06) * inv;
        out[14] = (a31 * b01 - a30 * b03 - a32 * b00) * inv;
        out[15] = (a20 * b03 - a21 * b01 + a22 * b00) * inv;
        return true;
    }
}
