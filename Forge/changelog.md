v3.0.12

### Changes
- **Added New Easing Types**:
    - **Bezier Easing**: Implements smooth transitions using cubic Bézier curves for precise animation control. Adapted from base code by ZigyTheBird.
    - **Bezier After Easing**: A variation of Bezier easing, applied to animations after a specified point in time.
- **Ticking Light Block Enhancements**:
    - Added support for variable light-level emission via the `LIGHT_LEVEL` block state property, allowing the block to emit dynamic levels of light ranging from 0–15 depending on its state.
    - The block is now fully waterloggable, allowing it to function correctly even when submerged in water.

### Fixes
- Fixed an issue where armor did not properly scale to match the scaling of model parts.
- Fixed an issue using the DynamicModelRenderer, you were required to set a render type for the bone. The RenderType is now properly optional.

### Credits
- Special thanks to ZigyTheBird for contributing the foundational concepts and code for Bézier easing.