v3.1.15

### Additions
- Added `AzWeightedPoolBehavior`, a play behavior that picks the next animation from a weighted pool each time the current one finishes, for idle or walk variety without timers.
  - The next pick happens when the animation actually ends, based on its real length, so resource packs that retime or replace animations keep working.
  - `addNoRepeat` stops an animation from playing twice in a row (useful for detail animations like a sniff or head turn), and `avoidImmediateRepeat()` applies this to every entry at once.
  - `randomSequence()` starts a pool on a weighted-random entry; `sequence()` always starts on the first entry or the one chosen with `startWith(...)`.
  - If a resource pack removes an animation, the pool skips it; if none of its animations can be found, the current animation keeps looping.

### Fixes
- 

### Developer Notes
- Pools must be built during common init so they're registered on both sides. Only the pool name is sent over the network, and if the client doesn't know it, the animation plays once and stops without an error. Use a namespaced name like `modid:pool_name`.
- Pools are meant to be the last stage of a sequence. Stages queued after a pool play after its first animation, and the pool doesn't pick in that case.
- Picks happen on each client, so different players may see different variants.
- Sending the pool that's already playing does nothing, so the same pool command can be sent every tick. Call `randomSequence()` once when entering a state and keep sending that command; calling it every tick restarts the pool.