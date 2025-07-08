package com.example.SafeMindAPI.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageResponseDTO<T> {
    private List<T> content;
    private int actualPage;
    private int totalPages;
    private long totalElements;
    private boolean last;

    public PageResponseDTO(Page<T> page) {
        this.content = page.getContent();
        this.actualPage = page.getNumber();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.last = page.isLast();
    }
}
