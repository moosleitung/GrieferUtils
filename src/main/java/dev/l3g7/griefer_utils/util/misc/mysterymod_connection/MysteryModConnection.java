package dev.l3g7.griefer_utils.util.misc.mysterymod_connection;

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.event.events.network.MysteryModConnectionEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent;
import dev.l3g7.griefer_utils.util.misc.mysterymod_connection.packets.PacketDecoder;
import dev.l3g7.griefer_utils.util.misc.mysterymod_connection.packets.PacketEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.DefaultThreadFactory;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutionException;

import static dev.l3g7.griefer_utils.util.misc.mysterymod_connection.MysteryModConnection.State.*;

public class MysteryModConnection {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final SocketAddress SERVER_HOST = new InetSocketAddress("server2.mysterymod.net", 5654);
	public static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4, new DefaultThreadFactory("GrieferUtils' MysteryMod-Connection", false, Thread.MIN_PRIORITY));

	private static State currentState = NOT_CONNECTED;

	public static State getState() {
		return currentState;
	}

	public static void setState(ChannelHandlerContext ctx, State state) {
		currentState = state;
		MinecraftForge.EVENT_BUS.post(new MysteryModConnectionEvent.MMStateChangeEvent(ctx, state));
	}

	@OnEnable
	private static void connect() {
		setState(null, CONNECTING);

		LOGGER.info("Connecting to {}", SERVER_HOST);
		Bootstrap bootstrap = new Bootstrap().group(eventLoopGroup).option(ChannelOption.TCP_NODELAY, true)
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<NioSocketChannel>() {
				protected void initChannel(NioSocketChannel ch) {
					ch.pipeline()
						.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4))
						.addLast(new PacketDecoder())
						.addLast(new LengthFieldPrepender(4))
						.addLast(new PacketEncoder())
						.addLast(new PacketHandler());
				}
			});

		try {
			bootstrap.connect(SERVER_HOST).get();
		} catch (NullPointerException e) {
			// Address could not be resolved, probably no internet
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	@EventListener
	public static void onServerJoin(ServerEvent.ServerJoinEvent event) {
		if (getState() != CONNECTED)
			connect();
	}

	public enum State {

		CONNECTED("Verbunden"),
		INVALID_SESSION("Die Session ist ungültig!"),
		SESSION_SERVERS_UNAVAILABLE("Die Session-Server sind nicht erreichbar!"),
		ERROR_OCCURRED("Es gab einen Fehler!"),
		CONNECTING("Verbindet..."),
		NOT_CONNECTED("Nicht verbunden");

		public final String errorMessage;
		State(String errorMessage) {
			this.errorMessage = errorMessage;
		}
	}

}
