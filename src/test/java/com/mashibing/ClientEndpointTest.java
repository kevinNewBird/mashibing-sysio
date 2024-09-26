package com.mashibing;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * description  ClientEndpointTest <BR>
 * <p>
 * author: zhao.song
 * date: created in 11:31  2023/3/3
 * company: TRS信息技术有限公司
 * version 1.0
 */
public class ClientEndpointTest {

    public static void main(String[] args) {
        sendPointClient();
    }

    private static void sendPointClient() {
        try {
            // open websocket
            final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI("ws://127.0.0.1:8080/ws/test"));

            // add listener
            clientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
                public void handleMessage(String message) {
                    System.out.println(message);
                }
            });

            // send message to websocket
            clientEndPoint.sendMessage("{'event':'addChannel','channel':'ok_btccny_ticker'}");

            // wait 5 seconds for messages from websocket
            Thread.sleep(5000);

        } catch (InterruptedException ex) {
            System.err.println("InterruptedException exception: " + ex.getMessage());
        } catch (URISyntaxException ex) {
            System.err.println("URISyntaxException exception: " + ex.getMessage());
        }
    }
}
