package com.example.user.config.common.codef.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateConnectedIdReqDto {

    List<AccountDto> accountList;


    public CreateConnectedIdReqDto() {
        this.accountList = new ArrayList<>();
    }

}
