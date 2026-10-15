package mod.azure.azurelib.util;

import java.util.Locale;
import java.util.Objects;

/**
 * Beta 1.7.3 has no namespaced resource identifier, so AzureLib carries its own. It keeps the 1.7.10 method names
 * ({@link #getResourceDomain()}, {@link #getResourcePath()}) as well as the modern ones so that code written against
 * either branch reads naturally.
 * <p>
 * A location {@code namespace:path} resolves to the classpath resource {@code /assets/namespace/path}. The
 * {@code minecraft} namespace is special-cased by {@link #toClasspathPath()}: Beta's own assets live at the root of the
 * client jar (e.g. {@code /mob/zombie.png}, {@code /misc/glint.png}), so {@code minecraft:mob/zombie.png} resolves to
 * {@code /mob/zombie.png}.
 */
public final class ResourceLocation implements Comparable<ResourceLocation> {

    public static final String DEFAULT_NAMESPACE = "minecraft";

    private final String namespace;

    private final String path;

    public ResourceLocation(String namespace, String path) {
        this.namespace = (namespace == null || namespace.isEmpty() ? DEFAULT_NAMESPACE : namespace).toLowerCase(
            Locale.ROOT
        );
        this.path = Objects.requireNonNull(path, "path");
    }

    public ResourceLocation(String location) {
        int colon = location.indexOf(':');
        if (colon >= 0) {
            this.namespace = colon == 0
                ? DEFAULT_NAMESPACE
                : location.substring(0, colon).toLowerCase(Locale.ROOT);
            this.path = location.substring(colon + 1);
        } else {
            this.namespace = DEFAULT_NAMESPACE;
            this.path = location;
        }
    }

    public static ResourceLocation of(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    public static ResourceLocation parse(String location) {
        return new ResourceLocation(location);
    }

    public String getResourceDomain() {
        return this.namespace;
    }

    public String getResourcePath() {
        return this.path;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getPath() {
        return this.path;
    }

    public ResourceLocation withPath(String newPath) {
        return new ResourceLocation(this.namespace, newPath);
    }

    /**
     * The absolute classpath path of this resource, e.g. {@code /assets/azurelib/geo/foo.geo.json}.
     */
    public String toClasspathPath() {
        String p = this.path.startsWith("/") ? this.path.substring(1) : this.path;
        if (DEFAULT_NAMESPACE.equals(this.namespace)) {
            // Vanilla Beta assets live at the jar root; also accept modern-style "textures/..." prefixes.
            return "/" + (p.startsWith("textures/") ? p.substring("textures/".length()) : p);
        }
        return "/assets/" + this.namespace + "/" + p;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ResourceLocation))
            return false;
        ResourceLocation other = (ResourceLocation) o;
        return this.namespace.equals(other.namespace) && this.path.equals(other.path);
    }

    @Override
    public int hashCode() {
        return 31 * this.namespace.hashCode() + this.path.hashCode();
    }

    @Override
    public int compareTo(ResourceLocation o) {
        int c = this.path.compareTo(o.path);
        return c != 0 ? c : this.namespace.compareTo(o.namespace);
    }

    @Override
    public String toString() {
        return this.namespace + ":" + this.path;
    }
}
