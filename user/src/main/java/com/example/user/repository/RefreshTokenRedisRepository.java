package com.example.user.repository;

import com.example.user.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;


public interface RefreshTokenRedisRepository extends CrudRepository<RefreshToken, String> {



}
