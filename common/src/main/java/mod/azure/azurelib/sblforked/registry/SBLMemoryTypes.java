/**
 * This class is a fork of the matching class found in the SmartBrainLib repository. Original source:
 * https://github.com/Tslat/SmartBrainLib Copyright © 2024 Tslat. Licensed under Mozilla Public License 2.0:
 * https://github.com/Tslat/SmartBrainLib/blob/1.21/LICENSE.
 */
package mod.azure.azurelib.sblforked.registry;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import mod.azure.azurelib.sblforked.SBLConstants;

/**
 * Registry class for custom {@link MemoryModuleType Memory Types}
 */
public final class SBLMemoryTypes {

    public static void init() {}

    public static final Supplier<MemoryModuleType<List<Projectile>>> INCOMING_PROJECTILES = register(
        "incoming_projectiles"
    );

    public static final Supplier<MemoryModuleType<Boolean>> TARGET_UNREACHABLE = register("target_unreachable");

    public static final Supplier<MemoryModuleType<Boolean>> SPECIAL_ATTACK_COOLDOWN = register(
        "special_attack_cooldown"
    );

    public static final Supplier<MemoryModuleType<List<Pair<BlockPos, BlockState>>>> NEARBY_BLOCKS = register(
        "nearby_blocks"
    );

    public static final Supplier<MemoryModuleType<List<ItemEntity>>> NEARBY_ITEMS = register("nearby_items");

    private static <T> Supplier<MemoryModuleType<T>> register(String id) {
        return register(id, Optional.empty());
    }

    private static <T> Supplier<MemoryModuleType<T>> register(String id, Optional<Codec<T>> codec) {
        return SBLConstants.SBL_LOADER.registerMemoryType(id, codec);
    }
}
