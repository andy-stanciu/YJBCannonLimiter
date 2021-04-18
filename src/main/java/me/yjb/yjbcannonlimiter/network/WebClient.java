package me.yjb.yjbcannonlimiter.network;

import me.yjb.yjbcannonlimiter.YJBCannonLimiter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

public class WebClient extends WebSocketClient
{
    private final YJBCannonLimiter core;

    public WebClient(URI uri, YJBCannonLimiter core)
    {
        super(uri);
        this.core = core;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {}

    @Override
    public void onClose(int code, String reason, boolean remote) {}

    @Override
    public void onMessage(String message)
    {
        String[] information = message.split("/");
        if (information.length == 1)
        {
            this.core.setValid(false);
        }
        else
        {
            this.core.setValid(information[0].trim().equalsIgnoreCase("1"));
            this.core.setClientName(information[1].trim());
        }
    }

    @Override
    public void onMessage(ByteBuffer message) {}

    @Override
    public void onError(Exception e) { e.printStackTrace(); }
}
