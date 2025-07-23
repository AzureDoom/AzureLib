v3.0.10

### Bug Fixes
- Resolved an issue where the animation controller incorrectly defaulted to `Linear` easing when the `setEasingType` method was not explicitly overridden at the controller level.
  - It now defaults to `null`, ensuring that easing types defined in the JSON are used as intended.