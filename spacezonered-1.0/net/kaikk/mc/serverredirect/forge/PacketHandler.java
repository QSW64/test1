package net.kaikk.mc.serverredirect.forge;

import net.minecraftforge.network.simple.*;
import java.util.regex.*;
import java.util.*;
import net.minecraft.network.*;
import java.nio.charset.*;
import java.util.function.*;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.fml.*;
import net.minecraft.resources.*;
import net.minecraftforge.network.*;

public class PacketHandler
{
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel REDIRECT_CHANNEL;
    public static final SimpleChannel FALLBACK_CHANNEL;
    public static final SimpleChannel ANNOUNCE_CHANNEL;
    public static final Pattern ADDRESS_PREVALIDATOR;
    public static final Object EMPTY_OBJECT;
    
    public static void init() {
        PacketHandler.REDIRECT_CHANNEL.registerMessage(0, (Class)String.class, (BiConsumer)PacketHandler::encode, (Function)PacketHandler::decode, (BiConsumer)PacketHandler::handleRedirect, (Optional)Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        PacketHandler.FALLBACK_CHANNEL.registerMessage(0, (Class)String.class, (BiConsumer)PacketHandler::encode, (Function)PacketHandler::decode, (BiConsumer)PacketHandler::handleFallback, (Optional)Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        PacketHandler.ANNOUNCE_CHANNEL.registerMessage(0, (Class)Object.class, (BiConsumer)PacketHandler::encodeVoid, (Function)PacketHandler::decodeVoid, (BiConsumer)PacketHandler::handleAnnounce, (Optional)Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
    
    public static void encode(final String addr, final FriendlyByteBuf buffer) {
        buffer.writeCharSequence((CharSequence)addr, StandardCharsets.UTF_8);
    }
    
    public static String decode(final FriendlyByteBuf buffer) {
        return buffer.toString(StandardCharsets.UTF_8);
    }
    
    public static void handleRedirect(final String addr, final Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT && PacketHandler.ADDRESS_PREVALIDATOR.matcher(addr).matches()) {
            ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ServerRedirect.redirect(addr)));
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handleFallback(final String addr, final Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT && PacketHandler.ADDRESS_PREVALIDATOR.matcher(addr).matches()) {
            ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ServerRedirect.setFallbackServerAddress(addr)));
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void encodeVoid(final Object v, final FriendlyByteBuf buffer) {
    }
    
    public static Object decodeVoid(final FriendlyByteBuf buffer) {
        return PacketHandler.EMPTY_OBJECT;
    }
    
    public static void handleAnnounce(final Object v, final Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ServerRedirect.players.add(ctx.get().getSender().m_20148_());
        }
        ctx.get().setPacketHandled(true);
    }
    
    static {
        REDIRECT_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("srvredirect", "red"), () -> "1", NetworkRegistry.acceptMissingOr("1"), NetworkRegistry.acceptMissingOr("1"));
        FALLBACK_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("srvredirect", "fal"), () -> "1", NetworkRegistry.acceptMissingOr("1"), NetworkRegistry.acceptMissingOr("1"));
        ANNOUNCE_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("srvredirect", "ann"), () -> "1", NetworkRegistry.acceptMissingOr("1"), NetworkRegistry.acceptMissingOr("1"));
        ADDRESS_PREVALIDATOR = Pattern.compile("^[A-Za-z0-9-_.:]+$");
        EMPTY_OBJECT = new Object();
    }
}
