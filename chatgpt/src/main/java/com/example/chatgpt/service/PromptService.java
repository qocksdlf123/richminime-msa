package com.example.chatgpt.service;

import com.example.chatgpt.dto.PromptReqDto;
import com.example.chatgpt.dto.PromptResDto;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PromptService {
    Mono<PromptResDto> findChatbotReply(PromptReqDto dto);

}
