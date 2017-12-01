package com.example.android.myapplication;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

class VoiceTransmitter extends WebSocketListener {
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    static final String TAG = "WSListener";
    boolean open = false;
    WebSocket ws;
    
    
    public void run() {
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request req = chain.request()
                                      .newBuilder()
                                      .addHeader("Sec-WebSocket-Protocol", "echo-protocol")
                                      .build();
                return chain.proceed(req);
            }
        };
        
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
        
        Request request = new Request.Builder()
                                  .url("ws://192.168.10.63")
                                  .build();
    
        client.newWebSocket( request, this);
        
        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown();
    }
    
    public boolean send(byte[] data) {
        
        return ws != null && ws.send(ByteString.of(data));
    }
    
    
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        output("opened");
        ws = webSocket;
        open = true;
    }
    
    @Override
    public void onMessage(WebSocket webSocket, String text) {
        output("Receiving : " + text);
    }
    
    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        output("Receiving bytes : " + bytes.hex());
    }
    
    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        output("Closing : " + code + " / " + reason);
        open = false;
        ws = null;
    }
    
    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        output("Error : " + t.getMessage());
    }
    
    void output(String s) {
        Log.d(TAG, s);
    }
    
    boolean isOpen() {
        return open;
    }
}