package com.example.android.myapplication.voice;


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

enum Header {
    RECOGNIZE("recognize");
    private final String name;
    Header(final String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
}

class VoiceTransmitter extends WebSocketListener {
    private static final String TAG = "VoiceTransmitter";
    private static String SUB_PROTOCOL_HEADER = "Sec-WebSocket-Protocol";
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private WebSocket ws;
    private int sampleRate;
    
    
    public VoiceTransmitter(int sampleRate) {
        this.sampleRate = sampleRate;
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request req = chain.request()
                                      .newBuilder()
                                      .addHeader(SUB_PROTOCOL_HEADER, Header.RECOGNIZE.getName())
                                      .build();
                return chain.proceed(req);
            }
        };
    
        OkHttpClient client = new OkHttpClient.Builder()
                                      .addInterceptor(interceptor)
                                      //タイムアウト
                                      .readTimeout(0, TimeUnit.MILLISECONDS)
                                      .build();
    
        Request request = new Request.Builder()
                                  .url("ws://192.168.10.24")
                                  .build();
        
        ws = client.newWebSocket( request, this);

//        client.dispatcher()
//                .executorService()
//                .shutdown();
    }
    
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        emitLog("opened");
        ws = webSocket;
    }
    
    @Override
    public void onMessage(WebSocket webSocket, String text) {
        emitLog("Receiving : " + text);
    }
    
    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        emitLog("Receiving bytes : " + bytes.hex());
    }
    
    //こちらが閉じた時も閉じられたときも実行される
    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        ws = null;
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        emitLog("Closing : " + code + " / " + reason);
    }
    
    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        emitLog("Error : " + t.getMessage());
    }
    
    private void emitLog(String s) {
        Log.d(TAG, s);
    }
    
    boolean send ( byte[] data ) {
//        return isOpen () && ws.send(Base64.encodeToString(data,Base64.DEFAULT));
        boolean result = isOpen () && ws.send(ByteString.of(data));
        if (!result) {
            Log.e(TAG, "could not queue audio data");
        }
        return result;
    }
    boolean send (final String message ) {
        return isOpen () && ws.send(message);
    }
    
    boolean stopRecognize() {
        return this.send ( "stopRecognize" );
    }
    
    private boolean isOpen () {
        return ws!=null;
    }
    
    boolean close () {
        //wsはonClosingでnullになるので問題ない
        return isOpen() && ws.close ( 1000, null ) ;
    }
}