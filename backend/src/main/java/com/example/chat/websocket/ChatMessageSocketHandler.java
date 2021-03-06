package com.example.chat.websocket;

import com.example.chat.entity.Message;
import com.example.chat.service.MessageService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
public class ChatMessageSocketHandler extends TextWebSocketHandler {
    private Logger logger = LoggerFactory.getLogger(ChatMessageSocketHandler.class);

    @Autowired
    private MessageService messageService;

    private List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage textMessage)
            throws InterruptedException, IOException {

        Message message = new Gson().fromJson(textMessage.getPayload(), Message.class);
        messageService.saveMessage(message);

        for (WebSocketSession webSocketSession : sessions) {
            if (webSocketSession.isOpen()) {
                webSocketSession.sendMessage(allMessagesToJson());
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);

        session.sendMessage(allMessagesToJson());
    }

    private TextMessage allMessagesToJson() {
        return new TextMessage(new Gson().toJson(messageService.getMessages()));
    }
}