v3.0.12

### Changes
- **Added New Easing Types**:
    - **Bezier Easing**: Implements smooth transitions using cubic Bézier curves for precise animation control. Adapted from base code by ZigyTheBird.
    - **Bezier After Easing**: A variation of Bezier easing, applied to animations after a specified point in time.

### Fixes
- Fixed an issue using the DynamicModelRenderer, you were required to set a render type for the bone. The RenderType is now properly optional.

### Credits
- Special thanks to ZigyTheBird for contributing the foundational concepts and code for Bézier easing.