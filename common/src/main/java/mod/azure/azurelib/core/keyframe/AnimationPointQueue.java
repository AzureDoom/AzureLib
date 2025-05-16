/**
 * This class is a fork of the matching class found in the Geckolib repository.
 * Original source: https://github.com/bernie-g/geckolib
 * Copyright © 2024 Bernie-G.
 * Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package mod.azure.azurelib.core.keyframe;

import java.io.Serial;
import java.util.LinkedList;

/**
 * An {@link AnimationPoint} queue holds a queue of {@code AnimationPoints} which are used in
 * the {@link mod.azure.azurelib.core.animation.AnimationController} to lerp between values
 */
@Deprecated(forRemoval = true)
public final class AnimationPointQueue extends LinkedList<AnimationPoint> {
	@Serial
	private static final long serialVersionUID = 5472797438476621193L;
}
