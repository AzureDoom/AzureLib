package mod.azure.azurelib.mixins.client;

import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.client.lang.AzModLang;
import mod.azure.azurelib.mixins.client.accessor.LanguageAccessor;

/** Adds every mod's lang file to Beta's translation table once vanilla's own files are loaded. */
@Mixin(Language.class)
public abstract class LanguageMixin_AzModLang {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void azurelib$loadModLang(CallbackInfo ci) {
        AzModLang.loadInto(((LanguageAccessor) this).azurelib$getTranslations());
    }
}
