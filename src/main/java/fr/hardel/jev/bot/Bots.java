package fr.hardel.jev.bot;

import com.mojang.authlib.GameProfile;
import fr.hardel.jev.Jev;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class Bots {
    private static final DisconnectionDetails LEAVE = new DisconnectionDetails(Component.literal("Jev bot removed"));
    private static final Map<UUID, Bot> bots = new ConcurrentHashMap<>();

    private Bots() {
    }

    public static UUID uuid(String name) {
        return UUID.nameUUIDFromBytes(("JevBot:" + name).getBytes(StandardCharsets.UTF_8));
    }

    public static @Nullable Bot get(String name) {
        return bots.get(uuid(name));
    }

    public static @Nullable Bot of(ServerPlayer player) {
        return bots.get(player.getUUID());
    }

    public static Collection<Bot> all() {
        return bots.values();
    }

    public static Bot spawn(MinecraftServer server, ServerLevel level, String name, Vec3 position, float yaw, Function<Bot, Brain> brain) {
        GameProfile profile = new GameProfile(uuid(name), name);
        ClientInformation information = information(server);
        ServerPlayer player = new ServerPlayer(server, level, profile, information);
        Ears ears = new Ears();
        server.getPlayerList().placeNewPlayer(new FakeConnection(ears), player, new CommonListenerCookie(profile, 0, information, false));
        player.snapTo(position.x, position.y, position.z, yaw, 0);
        level.getChunkSource().move(player);
        Bot bot = new Bot(player, ears, brain);
        bots.put(profile.id(), bot);
        Jev.LOGGER.info("Bot {} joined at [{}, {}, {}]", name, (int) position.x, (int) position.y, (int) position.z);
        return bot;
    }

    private static ClientInformation information(MinecraftServer server) {
        ClientInformation defaults = ClientInformation.createDefault();
        return new ClientInformation(defaults.language(), server.getPlayerList().getViewDistance(), defaults.chatVisibility(), defaults.chatColors(), defaults.modelCustomisation(),
            defaults.mainHand(), defaults.textFilteringEnabled(), defaults.allowsListing(), defaults.particleStatus());
    }

    public static void remove(Bot bot) {
        bots.remove(bot.player().getUUID());
        bot.player().connection.onDisconnect(LEAVE);
    }

    public static void forget() {
        bots.clear();
    }

    public static void tick(ServerPlayer player) {
        Bot bot = bots.get(player.getUUID());
        if (bot != null) {
            bot.tick();
        }
    }
}
