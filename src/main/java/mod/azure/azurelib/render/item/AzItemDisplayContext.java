package mod.azure.azurelib.render.item;

import net.minecraftforge.client.IItemRenderer;

/**
 * Where an item is being displayed. Keeps the names of 1.12.2's {@code ItemCameraTransforms.TransformType} so the
 * AzureLib API reads the same across versions; {@link #from(IItemRenderer.ItemRenderType)} maps 1.7.10's coarser
 * {@link IItemRenderer.ItemRenderType}.
 */
public enum AzItemDisplayContext {

    NONE("none"),
    THIRD_PERSON_LEFT_HAND("thirdperson_lefthand"),
    THIRD_PERSON_RIGHT_HAND("thirdperson_righthand"),
    FIRST_PERSON_LEFT_HAND("firstperson_lefthand"),
    FIRST_PERSON_RIGHT_HAND("firstperson_righthand"),
    HEAD("head"),
    GUI("gui"),
    GROUND("ground"),
    FIXED("fixed");

    private final String jsonKey;

    AzItemDisplayContext(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    /** The key of this context in a Blockbench / vanilla item model's {@code display} block. */
    public String jsonKey() {
        return this.jsonKey;
    }

    public boolean isFirstPerson() {
        return this == FIRST_PERSON_LEFT_HAND || this == FIRST_PERSON_RIGHT_HAND;
    }

    public static AzItemDisplayContext from(IItemRenderer.ItemRenderType type) {
        switch (type) {
            case ENTITY:
                return GROUND;
            case EQUIPPED:
                return THIRD_PERSON_RIGHT_HAND;
            case EQUIPPED_FIRST_PERSON:
                return FIRST_PERSON_RIGHT_HAND;
            case INVENTORY:
                return GUI;
            default:
                return NONE;
        }
    }
}
