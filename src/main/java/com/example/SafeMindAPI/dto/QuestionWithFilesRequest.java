package com.example.SafeMindAPI.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionWithFilesRequest {
    private String question;
    private List<String> fileIds;
}
