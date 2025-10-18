v3.2.0

***THIS IS A BREAKING CHANGE, PLEASE ENSURE THE MODS THAT USE AZURELIB ARE UPDATED FIRST TO THIS VERSION, PLEASE GIVE THEM TIME TO UPDATE***

### Changes
- Removed old Geckolib 3.x code.
- Layers, Model Renderers, and Animators now take a Key to identify themselves.
    - This will Long for Block Entities or UUID for Items (This includes Armor) and Entities.
- Adds AzItemRendererConfig#disableAnimationInAllContexts
- Per-instance model isolation for animations
    - Implemented AzBone.deepCopy() (copies transforms, matrices, cubes, children, then saveInitialSnapshot()).
    - Implemented AzBakedModel.deepCopy() (rebuilds a fresh bone tree via bone.deepCopy()).
- Deprecated pattern: do not call animator.setActiveModel(...) in the render loop (model is installed once during provideAnimator).
- Due to servers being weird with animations, AzIdentityRegistry#regsiter is required again to work properly with item and armor animations.
- Added fallback geo model for missing models.
- Extended AzRendererConfig and its Builder to BiFunction<Entity, T, ResourceLocation> model/texture/render type location providers.
    - Enables entity-aware model/texture/render type selection across all render configurations.
    - Subclasses like AzArmorRendererConfig now automatically support entity–item–based model lookups without custom overrides.

### Fixes
- Fixed animation state leaking between instances that share the same geo model by isolating bones per animator.
- Fixed enableAnimationOnlyInContexts not working properly
- Fixed animation speed property not working properly
- Fixed Animated textures breaking with ~~Gankolib~~ Geckolib
- Fixed a crash with armor if a bone is missing from the armor model.