package cashwarden.modblacklister;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModBlacklister implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("modblacklister");
    private static final Path CONFIG_PATH = Path.of("config/modblacklister.json");
    private static final Set<String> BLACKLISTED_MODS = new HashSet<>();

    @Override
    public void onInitialize() {
        loadConfig();

        // Register a handler for the login query start
        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            // Request mod list from client using custom packet
            sender.sendPacket(Identifier.of("modblacklister", "mod_list"), PacketByteBufs.empty());
        });

        // Handle the mod list response
        ServerLoginNetworking.registerGlobalReceiver(Identifier.of("modblacklister", "mod_list"), (server, handler, understood, buf, synchronizer, sender) -> {
            if (!understood) {
                handler.disconnect(Text.of("You must install Mod Blacklister to join this server."));
                return;
            }

            // Read mod list from the packet
            Map<String, String> clientMods;
            try {
                clientMods = buf.readMap(PacketByteBuf::readString, PacketByteBuf::readString);
            } catch (Exception e) {
                handler.disconnect(Text.of("Failed to read mod list: " + e.getMessage()));
                return;
            }

            // Log mod list for debugging
            LOGGER.info("Client mod list: {}", clientMods);

            // Check if Mod Blacklister is installed
            if (!clientMods.containsKey("modblacklister")) {
                handler.disconnect(Text.of("You must install Mod Blacklister to join this server."));
                return;
            }

            // Check for blacklisted mods
            for (String modId : clientMods.keySet()) {
                if (BLACKLISTED_MODS.contains(modId)) {
                    handler.disconnect(Text.of("Blacklisted mod detected: " + modId));
                    return;
                }
            }
        });
    }

    private void loadConfig() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                Files.writeString(CONFIG_PATH, "[\"examplebadmod\", \"anotherbadmod\"]");
            }
            Gson gson = new Gson();
            BLACKLISTED_MODS.clear();
            BLACKLISTED_MODS.addAll(gson.fromJson(new FileReader(CONFIG_PATH.toFile()), new TypeToken<Set<String>>(){}.getType()));
        } catch (IOException e) {
            LOGGER.error("Failed to load config: {}", e.getMessage());
        }
    }
}