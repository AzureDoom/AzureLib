/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;

import mod.azure.azurelib.util.GsonHelper;
import mod.azure.azurelib.util.math.Direction;

/**
 * Container class for UV face information, only used in deserialization at startup
 */
public final class UVFaces {

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
        return this.north;
    }

    public FaceUV south() {
        return this.south;
    }

    public FaceUV east() {
        return this.east;
    }

    public FaceUV west() {
        return this.west;
    }

    public FaceUV up() {
        return this.up;
    }

    public FaceUV down() {
        return this.down;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UVFaces))
            return false;
        UVFaces other = (UVFaces) o;
        return java.util.Objects.equals(this.north, other.north)
            && java.util.Objects.equals(this.south, other.south)
            && java.util.Objects.equals(this.east, other.east)
            && java.util.Objects.equals(this.west, other.west)
            && java.util.Objects.equals(this.up, other.up)
            && java.util.Objects.equals(this.down, other.down);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.north);
        result = 31 * result + java.util.Objects.hashCode(this.south);
        result = 31 * result + java.util.Objects.hashCode(this.east);
        result = 31 * result + java.util.Objects.hashCode(this.west);
        result = 31 * result + java.util.Objects.hashCode(this.up);
        result = 31 * result + java.util.Objects.hashCode(this.down);
        return result;
    }

    @Override
    public String toString() {
        return "UVFaces[north=" + this.north + ", south=" + this.south + ", east=" + this.east + ", west=" + this.west
            + ", up=" + this.up + ", down=" + this.down + "]";
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
        switch ((direction)) {
            case NORTH:
                return north;
            case SOUTH:
                return south;
            case EAST:
                return east;
            case WEST:
                return west;
            case UP:
                return up;
            case DOWN:
                return down;
            default:
                throw new IllegalStateException();
        }
    }
}
