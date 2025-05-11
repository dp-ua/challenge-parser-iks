package com.dp_ua.iksparser.service;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PageableService {

    public <T> Page<T> getPage(List<T> content, int page, int size) {
        Pageable pageRequest = createPageRequest(page, size);
        return getPage(content, pageRequest);
    }

    public <T> Page<T> getPage(List<T> content, Pageable pageRequest) {
        int start = (int) pageRequest.getOffset();

        if (start >= content.size()) {
            return new PageImpl<>(Collections.emptyList(), pageRequest, content.size());
        }

        int end = Math.min(start + pageRequest.getPageSize(), content.size());
        List<T> pageContent = content.subList(start, end);
        return new PageImpl<>(pageContent, pageRequest, content.size());
    }

    public Pageable createPageRequest(int page, int size) {
        return PageRequest.of(page, size);
    }

    public Pageable createPageRequestWithSort(int page, int size, Sort sort) {
        return PageRequest.of(page, size, sort);
    }
}
