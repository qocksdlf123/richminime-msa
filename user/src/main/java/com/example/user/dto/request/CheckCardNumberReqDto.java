package com.example.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckCardNumberReqDto {

    String organization;

    String cardNumber;

    String uuid;

}
