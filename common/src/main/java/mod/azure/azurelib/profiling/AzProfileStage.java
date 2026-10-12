package mod.azure.azurelib.profiling;

/**
 * The pipeline stages AzureLib reports to {@link AzProfiler}. Stages nest: {@link #ANIMATE} runs inside
 * {@link #PRE_RENDER}, {@link #RE_RENDER} runs inside {@link #RENDER_LAYERS}, and so on.
 */
public enum AzProfileStage {
    /** One full {@code AzRendererPipeline.render} call. Subject: the animatable. */
    RENDER,
    /** Pre-render transforms, including animation for most renderer types. Subject: the animatable. */
    PRE_RENDER,
    /** The main model pass, bones and cubes to vertices. Subject: the animatable. */
    MODEL_RENDER,
    /** Render layers applied after the main model pass. Subject: the animatable. */
    RENDER_LAYERS,
    /** A layer re-render of the model. Subject: the animatable. */
    RE_RENDER,
    /** One full {@code AzAnimator.animate} call. Subject: the animatable. */
    ANIMATE,
    /** Molang query binding before controllers run. Subject: the animatable. */
    MOLANG_SETUP,
    /** One animation controller's update. Subject: the controller. */
    CONTROLLER_UPDATE,
    /** Applying the animated values to the bone cache. Subject: the animatable. */
    BONE_UPDATE,
    /** The animator's {@code setCustomAnimations} hook. Subject: the animatable. */
    CUSTOM_ANIMATIONS,
    /** Entity LOD evaluation. Subject: the entity. */
    LOD_UPDATE
}
