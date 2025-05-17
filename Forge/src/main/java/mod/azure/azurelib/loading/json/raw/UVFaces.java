/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;

/**
 * Container class for UV face information, only used in deserialization at startup
 */
public class UVFaces {

    private final FaceUV north;

    private final FaceUV south;

    private final FaceUV east;

    private final FaceUV west;

    private final FaceUV up;

    private final FaceUV down;

    public UVFaces(FaceUV north, FaceUV south, FaceUV east, FaceUV west, FaceUV up, FaceUV down) {
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
        this.up = up;
        this.down = down;
    }

    public FaceUV north() {
        return north;
    }

    public FaceUV south() {
        return south;
    }

    public FaceUV east() {
        return east;
    }

    public FaceUV west() {
        return west;
    }

    public FaceUV up() {
        return up;
    }

    public FaceUV down() {
        return down;
    }

    public static JsonDeserializer<UVFaces> deserializer() {
        return (json, type, context) -> {
            JsonObject obj = json.getAsJsonObject();
            FaceUV north = JSONUtils.deserializeClass(obj, "north", null, context, FaceUV.class);
            FaceUV south = JSONUtils.deserializeClass(obj, "south", null, context, FaceUV.class);
            FaceUV east = JSONUtils.deserializeClass(obj, "east", null, context, FaceUV.class);
            FaceUV west = JSONUtils.deserializeClass(obj, "west", null, context, FaceUV.class);
            FaceUV up = JSONUtils.deserializeClass(obj, "up", null, context, FaceUV.class);
            FaceUV down = JSONUtils.deserializeClass(obj, "down", null, context, FaceUV.class);

            return new UVFaces(north, south, east, west, up, down);
        };
    }

    public FaceUV fromDirection(Direction direction) {
        if (direction == Direction.NORTH) {
            return north;
        } else if (direction == Direction.SOUTH) {
            return south;
        } else if (direction == Direction.EAST) {
            return east;
        } else if (direction == Direction.WEST) {
            return west;
        } else if (direction == Direction.UP) {
            return up;
        } else {
            return down;
        }
    }
}
