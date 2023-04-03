package mod.azure.azurelib.loading.json.raw;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;

import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;

/**
 * Container class for UV face information, only used in deserialization at startup
 */
public class UVFaces {

	@Nullable
	public FaceUV north;
	@Nullable
	public FaceUV south;
	@Nullable
	public FaceUV east;
	@Nullable
	public FaceUV west;
	@Nullable
	public FaceUV up;
	@Nullable
	public FaceUV down;

	public UVFaces(@Nullable FaceUV north, @Nullable FaceUV south, @Nullable FaceUV east, @Nullable FaceUV west, @Nullable FaceUV up, @Nullable FaceUV down) {
		this.north = north;
		this.south = south;
		this.east = east;
		this.west = west;
		this.up = up;
		this.down = down;
	}

	@Nullable
	public FaceUV north() {
		return north;
	}

	@Nullable
	public FaceUV south() {
		return south;
	}

	@Nullable
	public FaceUV east() {
		return east;
	}

	@Nullable
	public FaceUV west() {
		return west;
	}

	@Nullable
	public FaceUV up() {
		return up;
	}

	@Nullable
	public FaceUV down() {
		return down;
	}

	public static JsonDeserializer<UVFaces> deserializer() {
		return (json, type, context) -> {
			JsonObject obj = json.getAsJsonObject();
			FaceUV north = GsonHelper.getAsObject(obj, "north", null, context, FaceUV.class);
			FaceUV south = GsonHelper.getAsObject(obj, "south", null, context, FaceUV.class);
			FaceUV east = GsonHelper.getAsObject(obj, "east", null, context, FaceUV.class);
			FaceUV west = GsonHelper.getAsObject(obj, "west", null, context, FaceUV.class);
			FaceUV up = GsonHelper.getAsObject(obj, "up", null, context, FaceUV.class);
			FaceUV down = GsonHelper.getAsObject(obj, "down", null, context, FaceUV.class);

			return new UVFaces(north, south, east, west, up, down);
		};
	}

	public FaceUV fromDirection(Direction direction) {
		if (direction.equals(Direction.NORTH))
			return north;
		else if (direction.equals(Direction.SOUTH))
			return south;
		else if (direction.equals(Direction.EAST))
			return east;
		else if (direction.equals(Direction.WEST))
			return west;
		else if (direction.equals(Direction.UP))
			return up;
		else
			return down;
	}
}
