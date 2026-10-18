package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
public class AiTestController {

    private final ChatClient chatClient;

    private final Map<String, ChatMemory> memories = new ConcurrentHashMap<>();

    public AiTestController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/ai/chat")
    public String chat(@RequestParam(defaultValue = "你好，请介绍一下你自己") String message) {
        log.info("收到请求 message={}", message);
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    @GetMapping(value = "/ai/stream")
    public Flux<String> stream(@RequestParam(defaultValue = "讲个笑话") String message) {
        log.info("收到请求 message={}", message);
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }

    @GetMapping("/ai/role")
    public String role(@RequestParam(defaultValue = "你好") String message) {
        log.info("收到请求 message={}", message);
        return chatClient.prompt()
                .system("你是一个专业的Java开发助手，回答要简洁专业")
                .user(message)
                .call()
                .content();
    }

    @GetMapping("/ai/memory")
    public String memory(@RequestParam String sessionId,
                         @RequestParam String message) {
        log.info("收到请求 sessionId={}, message={}", sessionId, message);
        // 为每个会话创建独立的记忆实例
        ChatMemory memory = memories.computeIfAbsent(sessionId, k ->
                MessageWindowChatMemory.builder()
                        .maxMessages(20)
                        .build()
        );

        // 使用 Builder 模式创建 Advisor
        return chatClient.prompt()
                .advisors(MessageChatMemoryAdvisor.builder(memory).build())
                .user(message)
                .call()
                .content();
    }



}
