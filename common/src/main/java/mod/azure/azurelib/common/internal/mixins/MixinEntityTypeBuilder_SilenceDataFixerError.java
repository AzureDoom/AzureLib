package mod.azure.azurelib.common.internal.mixins;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.*;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import mod.azure.azurelib.common.internal.common.registry.SilencedEntityTypeBuilder;

@Mixin(EntityType.Builder.class)
public class MixinEntityTypeBuilder_SilenceDataFixerError implements SilencedEntityTypeBuilder {

    @Shadow
    private EntityType.EntityFactory<Entity> factory;

    @Shadow
    private MobCategory category;

    @Shadow
    private ImmutableSet<Block> immuneTo;

    @Shadow
    private boolean serialize;

    @Shadow
    private boolean summon;

    @Shadow
    private boolean fireImmune;

    @Shadow
    private boolean canSpawnFarFromPlayer;

    @Shadow
    private int clientTrackingRange;

    @Shadow
    private int updateInterval;

    @Shadow
    private EntityDimensions dimensions;

    @Shadow
    private float spawnDimensionsScale;

    @Shadow
    private EntityAttachments.Builder attachments;

    @Shadow
    private FeatureFlagSet requiredFeatures;

    @Unique
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> EntityType<T> buildWithoutDataFixerCheck() {
        return new EntityType<>(
            (EntityType.EntityFactory<T>) this.factory,
            this.category,
            this.serialize,
            this.summon,
            this.fireImmune,
            this.canSpawnFarFromPlayer,
            this.immuneTo,
            this.dimensions.withAttachments(this.attachments),
            this.spawnDimensionsScale,
            this.clientTrackingRange,
            this.updateInterval,
            this.requiredFeatures
        );
    }
}
