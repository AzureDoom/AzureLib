v1.1.7

### Additions
- N/A

### Fixes
- N/A

### Changes
- Item animators now stay cached for 5 minutes without being rendered or dispatched to, up from 2. Stacks that go unrendered for a while, such as worn armor in first person, are less likely to lose their current animation before they're seen again.

### Developer Notes
- `AzIdentifiableItemStackAnimatorCache.ANIMATOR_IDLE_SECONDS` is now 300 (was 120), matching the 1.18.2, 1.20.1, 1.21.1 and 26.3 branches.
