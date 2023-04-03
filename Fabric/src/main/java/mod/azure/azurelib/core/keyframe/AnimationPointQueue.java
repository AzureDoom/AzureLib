/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package mod.azure.azurelib.core.keyframe;

import java.util.LinkedList;

/**
 * An {@link AnimationPoint} queue holds a queue of {@code AnimationPoints} which are used in
 * the {@link mod.azure.azurelib.core.animation.AnimationController} to lerp between values
 */
public final class AnimationPointQueue extends LinkedList<AnimationPoint> {
	private static final long serialVersionUID = 5472797438476621193L;
}
