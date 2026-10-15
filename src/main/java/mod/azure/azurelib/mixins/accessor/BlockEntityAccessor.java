package mod.azure.azurelib.mixins.accessor;

import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

/** Access to Beta's block entity class registry (MCP: TileEntity's static maps). */
@Mixin(BlockEntity.class)
public interface BlockEntityAccessor {

    @SuppressWarnings("rawtypes")
    @Invoker("register")
    static void azurelib$register(Class blockEntityClass, String id) {
        throw new AssertionError();
    }

    @SuppressWarnings("rawtypes")
    @Accessor("ID_TO_TYPE")
    static Map azurelib$getIdToClass() {
        throw new AssertionError();
    }

    @SuppressWarnings("rawtypes")
    @Accessor("TYPE_TO_ID")
    static Map azurelib$getClassToId() {
        throw new AssertionError();
    }
}
