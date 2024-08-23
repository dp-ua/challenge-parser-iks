package com.dp_ua.iksparser.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class PageableService {
    public <T> Page<T> getPage(List<T> content, int page, int size) {
        Pageable pageRequest = createPageRequest(page, size);
        return getPage(content, pageRequest);
    }

    public <T> Page<T> getPage(List<T> content, Pageable pageRequest) {
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), content.size());

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
