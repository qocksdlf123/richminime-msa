package com.example.chatgpt.service;

import com.example.chatgpt.dao.PromptRepository;
import com.example.chatgpt.domain.Prompt;
import com.example.chatgpt.dto.PromptReqDto;
import com.example.chatgpt.dto.PromptResDto;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PromptServiceImpl implements PromptService {

    private final PromptRepository promptRepository;
//    private final SecurityUtils securityUtils;
    @Value("${gpt.api-key}")
    private String apikey;

    public final String gptUrl = "https://api.openai.com/v1/chat/completions";
    @Override
    public Mono<PromptResDto> findChatbotReply(PromptReqDto dto) {
        WebClient webClient = WebClient.create(gptUrl);
        HttpHeaders httpheaders = new HttpHeaders();
        httpheaders.setContentType(MediaType.APPLICATION_JSON);
        httpheaders.set("Authorization","Bearer " + apikey);
        //추후 시큐리티 완료 되면 getUser()로 대체
//        List<Prompt> prompts = promptRepository.findByUser_UserId(securityUtils.getUserNo());
        List<Prompt> prompts = promptRepository.findByUserId(1L);     //임시 대체, 추후 user 마이크로서비스 구현 시 FeignClinet로 호출

        List<Message> messages = new ArrayList<>();
        Message systemMessage = new Message("system","너는 은행 상담원이야");
        messages.add(systemMessage);
        Message userMessage;
        Message assistantMessage;
        if(prompts != null){
            for(Prompt prompt : prompts){
                userMessage = new Message("user",prompt.getRoleUser());
                messages.add(userMessage);
                assistantMessage = new Message("assistant",prompt.getRoleContent());
                messages.add(assistantMessage);
            }
        }
        userMessage = new Message("user",dto.getRequest() + "글자수는 50글자 내외로 해줘");
        messages.add(userMessage);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model","gpt-3.5-turbo");
        requestBody.put("messages",messages);

        System.out.println(requestBody);


        return webClient.post()
                .headers(headers -> headers.addAll(httpheaders)) //헤더 설정(콜백 함수로 headers를 받아 headers에 Httpheaders 설정
                .bodyValue(requestBody)//requestbody 설정
                .retrieve()
                .bodyToMono(String.class) //응답을 string으로 받겠다는 설정
                .map(
                        result ->{
                            System.out.println(result);
                            System.out.println("성공");

                            return PromptResDto.builder().response(result).build();
                        });
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class Message{
        String role;
        String content;

    }
}