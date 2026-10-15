package mod.azure.azurelib.mixins.accessor;

import net.minecraft.entity.Entities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(Entities.class)
public interface EntityRegistryAccessor {

    @SuppressWarnings("rawtypes")
    @Invoker("register")
    static void azurelib$register(Class entityClass, String name, int id) {
        throw new AssertionError();
    }

    @SuppressWarnings("rawtypes")
    @Accessor("TYPE_TO_KEY")
    static Map azurelib$getNameToClass() {
        throw new AssertionError();
    }

    @SuppressWarnings("rawtypes")
    @Accessor("TYPE_TO_ID")
    static Map azurelib$getRawIdToClass() {
        throw new AssertionError();
    }
}
