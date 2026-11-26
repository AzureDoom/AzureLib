package mod.azure.azurelib.render;

public final class AzRenderContext {

    private static int levelExtractDepth;

    private AzRenderContext() {}

    public static void beginLevelExtract() {
        levelExtractDepth++;
    }

    public static void endLevelExtract() {
        if (levelExtractDepth > 0) {
            levelExtractDepth--;
        }
    }

    public static boolean isGuiRender() {
        return levelExtractDepth == 0;
    }
}
