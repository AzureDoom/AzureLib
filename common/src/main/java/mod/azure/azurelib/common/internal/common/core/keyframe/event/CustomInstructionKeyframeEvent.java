/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package mod.azure.azurelib.common.internal.common.core.keyframe.event;

import mod.azure.azurelib.common.internal.common.core.animatable.GeoAnimatable;
import mod.azure.azurelib.common.internal.common.core.animation.AnimationController;
import mod.azure.azurelib.common.internal.common.core.keyframe.event.data.CustomInstructionKeyframeData;

/**
 * The {@link KeyFrameEvent} specific to the {@link AnimationController#customKeyframeHandler}.<br>
 * Called when a custom instruction keyframe is encountered
 */
public class CustomInstructionKeyframeEvent<T extends GeoAnimatable> extends KeyFrameEvent<T, CustomInstructionKeyframeData> {
	public CustomInstructionKeyframeEvent(T entity, double animationTick, AnimationController<T> controller,
										  CustomInstructionKeyframeData customInstructionKeyframeData) {
		super(entity, animationTick, controller, customInstructionKeyframeData);
	}
	/**
	 * Get the {@link CustomInstructionKeyframeData} relevant to this event call
	 */
	@Override
	public CustomInstructionKeyframeData getKeyframeData() {
		return super.getKeyframeData();
	}
}
