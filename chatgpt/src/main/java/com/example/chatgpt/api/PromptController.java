package com.example.chatgpt.api;

import com.example.chatgpt.dto.PromptReqDto;
import com.example.chatgpt.dto.PromptResDto;
import com.example.chatgpt.service.PromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/prompt")
public class PromptController {

    private final PromptService promptService;

    @PostMapping
    public Mono<PromptResDto> findChatbotReply(@RequestBody PromptReqDto dto){
        return promptService.findChatbotReply(dto);
    }
}
