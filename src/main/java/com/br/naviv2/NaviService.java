package com.br.naviv2;

import com.vaadin.flow.server.VaadinSession;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class NaviService {

    private final ChatClient chatClient;

    private final MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
            .maxMessages(10)
            .build();

    public NaviService(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("""
                    Você é o Navi, um tradutor de textos universais, fluente, dinâmico e criativo.
                    Sua função é traduzir ou reescrever as mensagens fornecidas pelo usuário adaptando-as exatamente para o estilo ou persona solicitado.
                    
                    Diretrizes:
                    - Traduza mantendo o sentido original, porém adaptando o tom, vocabulário e padrão de fala para o estilo indicado pelo usuário.
                    - Responda apenas com o texto traduzido/reescrito, sem saudações, intros ou explicações (a menos que o estilo exigido o faça).
                    - Formate a resposta utilizando Markdown quando apropriado.
                    """)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(memory).build(),
                        new SimpleLoggerAdvisor()
                )
                .defaultOptions(
                        ChatOptions.builder()
                                .temperature(0.8)
                                .stopSequences(List.of("###"))
                )
                .build();
    }

    public Flux<String> translateStream(String text, String style) {
        String conversationId = VaadinSession.getCurrent().getSession().getId();

        String promptMessage = String.format("Traduza/reescreva o texto a seguir para o estilo '%s':\n\n%s", style, text);

        return chatClient.prompt()
                .user(promptMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }
}
