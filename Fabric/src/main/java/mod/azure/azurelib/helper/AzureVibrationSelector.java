package mod.azure.azurelib.helper;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class AzureVibrationSelector {
	public static final Codec<AzureVibrationSelector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			AzureVibrationInfo.CODEC.optionalFieldOf("event")
					.forGetter(vibrationSelector -> vibrationSelector.currentVibrationData.map(Pair::getLeft)),
			(Codec.LONG.fieldOf("tick")).forGetter(
					vibrationSelector -> vibrationSelector.currentVibrationData.map(Pair::getRight).orElse(-1L)))
			.apply(instance, AzureVibrationSelector::new));
	private Optional<Pair<AzureVibrationInfo, Long>> currentVibrationData;

	public AzureVibrationSelector(Optional<AzureVibrationInfo> optional, long l) {
		this.currentVibrationData = optional.map(vibrationInfo -> Pair.of(vibrationInfo, l));
	}

	public AzureVibrationSelector() {
		this.currentVibrationData = Optional.empty();
	}

	public void addCandidate(AzureVibrationInfo vibrationInfo, long l) {
		if (this.shouldReplaceVibration(vibrationInfo, l))
			this.currentVibrationData = Optional.of(Pair.of(vibrationInfo, l));
	}

	private boolean shouldReplaceVibration(AzureVibrationInfo vibrationInfo, long l) {
		if (this.currentVibrationData.isEmpty())
			return true;
		var pair = this.currentVibrationData.get();
		var m = pair.getRight();

		if (l != m)
			return false;

		var vibrationInfo2 = pair.getLeft();

		if (vibrationInfo.distance() < vibrationInfo2.distance())
			return true;
		if (vibrationInfo.distance() > vibrationInfo2.distance())
			return false;

		return AzureVibrationListener.getGameEventFrequency(vibrationInfo.gameEvent()) > AzureVibrationListener
				.getGameEventFrequency(vibrationInfo2.gameEvent());
	}

	public Optional<AzureVibrationInfo> chosenCandidate(long l) {
		if (this.currentVibrationData.isEmpty())
			return Optional.empty();
		if (this.currentVibrationData.get().getRight() < l)
			return Optional.of(this.currentVibrationData.get().getLeft());
		return Optional.empty();
	}

	public void startOver() {
		this.currentVibrationData = Optional.empty();
	}
}
