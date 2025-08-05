v3.0.27

### Changes
- Added new utility methods in ClientUtils. 
  - getCurrentAnimationController(); Allows you to get the controller of the animated object.
  - getCurrentAnimationTick(); Allows you to get the current tick of the current animation.
  - getCurrentAnimationLength(); Allows you to get the length of the current animation.

### Fixes
- Fixed an issue where NeoForge would kill itself on packets.

v3.0.26

### Changes
- Added an AzCommand create option for setting speed is now easier.
- Added an AzCommand create option that allows you to set how many ticks you'd like to start within the animation. (Ex: I want to start from the 2 second mark so 40 ticks instead of the start of the animation).

### Fixes
- Fixed an issue where setting animation speed via AzCommand would not take effect.