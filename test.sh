#!/usr/bin/env bash

GRADLE_CACHE="$HOME/.gradle/caches/fabric-loom/minecraftMaven"

find "$GRADLE_CACHE" \
    -type f \
    -name '*.jar' \
    -path '*b1.7.3*' \
    -print0 |
while IFS= read -r -d '' jar; do
    if unzip -Z -1 "$jar" 2>/dev/null |
       grep -qx 'net/minecraft/server/network/handler/ServerPlayNetworkHandler.class'; then
        echo "Found: $jar"
        echo
        echo "=== handleInteractEntity bytecode ==="

        javap -c -p -classpath "$jar" \
            net.minecraft.server.network.handler.ServerPlayNetworkHandler |
            sed -n '/handleInteractEntity(/,/handlePlayerRespawn(/p'

        exit 0
    fi
done