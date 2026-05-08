package cashwarden.modblacklister;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ClientModBlacklister implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLoginNetworking.registerGlobalReceiver(Identifier.of("modblacklister", "mod_list"), (client, handler, buf, listenerAdder) -> {
            Map<String, String> modList = new HashMap<>();
            // Populate with all loaded mods
            FabricLoader.getInstance().getAllMods().forEach(mod -> modList.put(mod.getMetadata().getId(), mod.getMetadata().getVersion().getFriendlyString()));
            PacketByteBuf responseBuf = PacketByteBufs.create();
            responseBuf.writeMap(modList, PacketByteBuf::writeString, PacketByteBuf::writeString);
            return CompletableFuture.completedFuture(responseBuf);
        });
    }
}