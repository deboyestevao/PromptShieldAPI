package com.example.SafeMindAPI.dto;


import lombok.Data;

import java.util.List;

@Data
public class QuestionYearMonthRequest {
    private List<QuestionDTO> questions;
    private Integer year;
    private Integer month;
}
