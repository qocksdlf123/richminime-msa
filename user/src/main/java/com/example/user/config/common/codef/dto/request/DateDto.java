package com.example.user.config.common.codef.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DateDto {

    private int year;

    private int month;

    private int day;

}
