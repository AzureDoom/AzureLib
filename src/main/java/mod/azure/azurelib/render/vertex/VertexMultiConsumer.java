package mod.azure.azurelib.render.vertex;

/**
 * Forwards every vertex element to two consumers, mirroring the 1.18
 * {@code com.mojang.blaze3d.vertex.VertexMultiConsumer.Double}. Used for enchantment glint passes.
 */
public final class VertexMultiConsumer {

    private VertexMultiConsumer() {
        throw new UnsupportedOperationException();
    }

    public static VertexConsumer create(VertexConsumer first, VertexConsumer second) {
        return new Double(first, second);
    }

    public static final class Double implements VertexConsumer {

        public final VertexConsumer first;

        public final VertexConsumer second;

        public Double(VertexConsumer first, VertexConsumer second) {
            if (first == second) {
                throw new IllegalArgumentException("Duplicate delegates");
            }
            this.first = first;
            this.second = second;
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            this.first.vertex(x, y, z);
            this.second.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            this.first.color(red, green, blue, alpha);
            this.second.color(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            this.first.uv(u, v);
            this.second.uv(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            this.first.overlayCoords(u, v);
            this.second.overlayCoords(u, v);
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            this.first.uv2(u, v);
            this.second.uv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            this.first.normal(x, y, z);
            this.second.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            this.first.endVertex();
            this.second.endVertex();
        }

        @Override
        public void vertex(
            float x,
            float y,
            float z,
            float red,
            float green,
            float blue,
            float alpha,
            float texU,
            float texV,
            int overlayUV,
            int lightmapUV,
            float normalX,
            float normalY,
            float normalZ
        ) {
            this.first.vertex(
                x,
                y,
                z,
                red,
                green,
                blue,
                alpha,
                texU,
                texV,
                overlayUV,
                lightmapUV,
                normalX,
                normalY,
                normalZ
            );
            this.second.vertex(
                x,
                y,
                z,
                red,
                green,
                blue,
                alpha,
                texU,
                texV,
                overlayUV,
                lightmapUV,
                normalX,
                normalY,
                normalZ
            );
        }
    }
}
