v3.1.6

### Changes
- Added AzRendererConfig builder for setting a custom model renderer.
- Added AzRendererConfig builder for setting a custom pipeline context renderer.
- Removed the need for AzIdentityRegistry#register to be called, any item with an AzItemRender/AzArmorRender will be automatically registered the needed identity.
- Added AzRendererConfig builder for setting per bone textures.
- Added AzRendererConfig builder for setting per bone RenderTypes.