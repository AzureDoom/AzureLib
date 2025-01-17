/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.common.loading.json;

import com.google.gson.annotations.SerializedName;

/**
 * Geo format version enum, mostly just used in deserialization at startup
 */
public enum FormatVersion {
    @SerializedName("1.12.0")
    V_1_12_0,
    @SerializedName("1.14.0")
    V_1_14_0,
    @SerializedName("1.21.0")
    V_1_21_0
}
