/**
 * This class is a fork of the matching class found in the SmartBrainLib repository. Original source:
 * https://github.com/Tslat/SmartBrainLib Copyright © 2024 Tslat. Licensed under Mozilla Public License 2.0:
 * https://github.com/Tslat/SmartBrainLib/blob/1.21/LICENSE.
 */
package mod.azure.azurelib.sblforked.api.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

import mod.azure.azurelib.sblforked.api.core.sensor.ExtendedSensor;
import mod.azure.azurelib.sblforked.api.core.sensor.PredicateSensor;
import mod.azure.azurelib.sblforked.object.FixedNearestVisibleLivingEntities;
import mod.azure.azurelib.sblforked.object.SquareRadius;
import mod.azure.azurelib.sblforked.registry.SBLSensors;
import mod.azure.azurelib.sblforked.util.BrainUtils;
import mod.azure.azurelib.sblforked.util.EntityRetrievalUtil;

/**
 * A sensor that looks for nearby living entities in the surrounding area, sorted by proximity to the brain owner.<br>
 * Defaults:
 * <ul>
 * <li>Radius is equivalent to the entity's {@link Attributes#FOLLOW_RANGE} attribute</li>
 * <li>Only alive entities</li>
 * </ul>
 *
 * @param <E> The entity
 */
public class NearbyLivingEntitySensor<E extends LivingEntity> extends PredicateSensor<LivingEntity, E> {

    private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(
        MemoryModuleType.NEAREST_LIVING_ENTITIES,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES
    );

    @Nullable
    protected SquareRadius radius = null;

    public NearbyLivingEntitySensor() {
        super((target, entity) -> target != entity && target.isAlive());
    }

    /**
     * Set the radius for the sensor to scan.
     *
     * @param radius The coordinate radius, in blocks
     * @return this
     */
    public NearbyLivingEntitySensor<E> setRadius(double radius) {
        return setRadius(radius, radius);
    }

    /**
     * Set the radius for the sensor to scan.
     *
     * @param xz The X/Z coordinate radius, in blocks
     * @param y  The Y coordinate radius, in blocks
     * @return this
     */
    public NearbyLivingEntitySensor<E> setRadius(double xz, double y) {
        this.radius = new SquareRadius(xz, y);

        return this;
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return SBLSensors.NEARBY_LIVING_ENTITY.get();
    }

    @Override
    protected void doTick(ServerLevel level, E entity) {
        SquareRadius radius = this.radius;

        if (radius == null) {
            double dist = entity.getAttributeValue(Attributes.FOLLOW_RANGE);

            radius = new SquareRadius(dist, dist);
        }

        List<LivingEntity> entities = EntityRetrievalUtil.getEntities(
            level,
            entity.getBoundingBox().inflate(radius.xzRadius(), radius.yRadius(), radius.xzRadius()),
            obj -> obj instanceof LivingEntity livingEntity && predicate().test(livingEntity, entity)
        );

        entities.sort(Comparator.comparingDouble(entity::distanceToSqr));

        BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES, entities);
        BrainUtils.setMemory(
            entity,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            new FixedNearestVisibleLivingEntities(entity, entities)
        );
    }
}
