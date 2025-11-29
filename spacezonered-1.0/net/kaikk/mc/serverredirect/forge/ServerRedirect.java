package net.kaikk.mc.serverredirect.forge;

import net.minecraftforge.fml.common.*;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.fml.javafmlmod.*;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.common.*;
import net.minecraftforge.fml.*;
import com.mojang.brigadier.builder.*;
import com.mojang.brigadier.tree.*;
import java.util.function.*;
import net.minecraft.server.level.*;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.*;
import com.mojang.brigadier.arguments.*;
import net.minecraftforge.event.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.multiplayer.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraft.network.chat.*;
import net.minecraft.client.multiplayer.resolver.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.gui.screens.*;
import net.minecraftforge.network.*;
import net.minecraftforge.server.*;
import net.minecraft.server.players.*;
import net.kaikk.mc.serverredirect.forge.event.*;
import java.util.*;
import com.mojang.brigadier.context.*;
import net.minecraft.commands.arguments.selector.*;
import com.mojang.brigadier.exceptions.*;
import org.apache.logging.log4j.*;

@Mod("serverredirect")
public class ServerRedirect
{
    public static final String MODID = "serverredirect";
    public static final Logger LOGGER;
    protected static final Set<UUID> players;
    @OnlyIn(Dist.CLIENT)
    public static volatile String fallbackServerAddress;
    @OnlyIn(Dist.CLIENT)
    public static boolean connected;
    
    public ServerRedirect() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener((Consumer)this::setup);
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        PacketHandler.init();
        MinecraftForge.EVENT_BUS.register((Object)this);
        ModLoadingContext.get().registerExtensionPoint((Class)IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "OHNOES\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31", (a, b) -> 1));
    }
    
    @SubscribeEvent
    public void onRegisterCommands(final RegisterCommandsEvent event) {
        event.getDispatcher().register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("redirect").requires(cs -> cs.m_6761_(2))).redirect((CommandNode)event.getDispatcher().register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("serverredirect").requires(cs -> cs.m_6761_(2))).then((ArgumentBuilder)this.commandAddress(ServerRedirect::sendTo)))));
        event.getDispatcher().register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("fallback").requires(cs -> cs.m_6761_(2))).redirect((CommandNode)event.getDispatcher().register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("fallbackserver").requires(cs -> cs.m_6761_(2))).then((ArgumentBuilder)this.commandAddress(ServerRedirect::sendFallbackTo)))));
        event.getDispatcher().register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("ifplayercanredirect").requires(cs -> cs.m_6761_(2))).then((ArgumentBuilder)this.commandIfPlayerRedirect(false)));
        event.getDispatcher().register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("ifplayercannotredirect").requires(cs -> cs.m_6761_(2))).then((ArgumentBuilder)this.commandIfPlayerRedirect(true)));
    }
    
    private ArgumentBuilder<CommandSourceStack, ?> commandAddress(final BiConsumer<ServerPlayer, String> consumer) {
        return (ArgumentBuilder<CommandSourceStack, ?>)Commands.m_82129_("Player(s)", (ArgumentType)EntityArgument.m_91470_()).then(Commands.m_82129_("Server Address", (ArgumentType)StringArgumentType.greedyString()).executes(cs -> {
            try {
                final String addr = (String)cs.getArgument("Server Address", (Class)String.class);
                if (!PacketHandler.ADDRESS_PREVALIDATOR.matcher(addr).matches()) {
                    ((CommandSourceStack)cs.getSource()).m_81352_((Component)Component.m_237113_("Invalid Server Address"));
                    return 0;
                }
                ((EntitySelector)cs.getArgument("Player(s)", (Class)EntitySelector.class)).m_121166_((CommandSourceStack)cs.getSource()).forEach(p -> {
                    try {
                        consumer.accept(p, addr);
                    }
                    catch (final Exception e2) {
                        e2.printStackTrace();
                    }
                    return;
                });
            }
            catch (final Exception e) {
                e.printStackTrace();
            }
            return 0;
        }));
    }
    
    private ArgumentBuilder<CommandSourceStack, ?> commandIfPlayerRedirect(final boolean not) {
        return (ArgumentBuilder<CommandSourceStack, ?>)Commands.m_82129_("Player(s)", (ArgumentType)EntityArgument.m_91470_()).then(Commands.m_82129_("Command...", (ArgumentType)StringArgumentType.greedyString()).executes(cs -> {
            try {
                final String command = (String)cs.getArgument("Command...", (Class)String.class);
                ((EntitySelector)cs.getArgument("Player(s)", (Class)EntitySelector.class)).m_121166_((CommandSourceStack)cs.getSource()).forEach(p -> {
                    try {
                        if (isUsingServerRedirect(p) != not) {
                            ((CommandSourceStack)cs.getSource()).m_81377_().m_129892_().m_230957_((CommandSourceStack)cs.getSource(), command.replace("%PlayerName", p.m_36316_().getName()).replace("%PlayerId", p.m_20149_()));
                        }
                    }
                    catch (final Exception e2) {
                        e2.printStackTrace();
                    }
                    return;
                });
            }
            catch (final Exception e) {
                e.printStackTrace();
            }
            return 0;
        }));
    }
    
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        final Minecraft mc = Minecraft.m_91087_();
        if (ServerRedirect.connected != (mc.f_91073_ != null)) {
            ServerRedirect.connected = (mc.f_91073_ != null);
            if (ServerRedirect.connected) {
                PacketHandler.ANNOUNCE_CHANNEL.sendToServer(PacketHandler.EMPTY_OBJECT);
            }
        }
        else if (ServerRedirect.fallbackServerAddress != null) {
            if (mc.f_91080_ instanceof DisconnectedScreen) {
                final String addr = ServerRedirect.fallbackServerAddress;
                ServerRedirect.fallbackServerAddress = null;
                redirect(addr);
            }
            else if (mc.f_91080_ instanceof TitleScreen || mc.f_91080_ instanceof JoinMultiplayerScreen) {
                ServerRedirect.fallbackServerAddress = null;
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        ServerRedirect.players.remove(event.getEntity().m_20148_());
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void redirect(final String serverAddress) {
        if (!Minecraft.m_91087_().m_18695_()) {
            throw new IllegalStateException("Not in the main thread");
        }
        if (MinecraftForge.EVENT_BUS.post((Event)new ClientRedirectEvent(serverAddress))) {
            return;
        }
        ServerRedirect.LOGGER.info("Connecting to " + serverAddress);
        final Minecraft mc = Minecraft.m_91087_();
        if (mc.f_91073_ != null) {
            mc.f_91073_.m_7462_();
        }
        if (mc.m_91090_()) {
            mc.m_91320_((Screen)new GenericDirtMessageScreen((Component)Component.m_237115_("menu.savingLevel")));
        }
        else {
            mc.m_91399_();
        }
        mc.m_91152_((Screen)new JoinMultiplayerScreen((Screen)new TitleScreen()));
        ConnectScreen.m_278792_(mc.f_91080_, mc, ServerAddress.m_171864_(serverAddress), new ServerData(serverAddress, serverAddress, false), false);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static String getFallbackServerAddress() {
        return ServerRedirect.fallbackServerAddress;
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void setFallbackServerAddress(final String fallbackServerAddress) {
        if (MinecraftForge.EVENT_BUS.post((Event)new ClientFallbackEvent(fallbackServerAddress))) {
            return;
        }
        ServerRedirect.fallbackServerAddress = fallbackServerAddress;
    }
    
    public static boolean sendTo(final ServerPlayer player, final String serverAddress) {
        if (MinecraftForge.EVENT_BUS.post((Event)new PlayerRedirectEvent(player, serverAddress))) {
            return false;
        }
        PacketHandler.REDIRECT_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)serverAddress);
        return true;
    }
    
    public static void sendToAll(final String serverAddress) {
        final PlayerList pl = ServerLifecycleHooks.getCurrentServer().m_6846_();
        for (final ServerPlayer player : pl.m_11314_()) {
            if (!MinecraftForge.EVENT_BUS.post((Event)new PlayerRedirectEvent(player, serverAddress))) {
                PacketHandler.REDIRECT_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)serverAddress);
            }
        }
    }
    
    public static boolean sendFallbackTo(final ServerPlayer player, final String serverAddress) {
        if (MinecraftForge.EVENT_BUS.post((Event)new PlayerFallbackEvent(player, serverAddress))) {
            return false;
        }
        PacketHandler.FALLBACK_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)serverAddress);
        return true;
    }
    
    public static void sendFallbackToAll(final String serverAddress) {
        final PlayerList pl = ServerLifecycleHooks.getCurrentServer().m_6846_();
        for (final ServerPlayer player : pl.m_11314_()) {
            if (!MinecraftForge.EVENT_BUS.post((Event)new PlayerFallbackEvent(player, serverAddress))) {
                PacketHandler.FALLBACK_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)serverAddress);
            }
        }
    }
    
    public static boolean isUsingServerRedirect(final ServerPlayer player) {
        return isUsingServerRedirect(player.m_20148_());
    }
    
    public static boolean isUsingServerRedirect(final UUID playerId) {
        return ServerRedirect.players.contains(playerId);
    }
    
    public static void forEachPlayerUsingServerRedirect(final Consumer<UUID> consumer) {
        synchronized (ServerRedirect.players) {
            for (final UUID playerId : ServerRedirect.players) {
                consumer.accept(playerId);
            }
        }
    }
    
    public static Set<UUID> getPlayers() {
        return Collections.unmodifiableSet((Set<? extends UUID>)new HashSet<UUID>(ServerRedirect.players));
    }
    
    static {
        LOGGER = LogManager.getLogger();
        players = Collections.synchronizedSet(new HashSet<UUID>());
    }
}
