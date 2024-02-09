package com.example.chatgpt.dao;

import com.example.chatgpt.domain.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PromptRepository extends JpaRepository<Prompt, Long> {
    @Query("select p from Prompt p where p.userId=:userId")
    List<Prompt> findByUserId(Long userId);

}
