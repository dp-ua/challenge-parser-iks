package com.dp_ua.iksparser.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class PageableService {
    public <T> Page<T> getPage(int page, int size, List<T> content) {
        Pageable pageRequest = createPageRequestUsing(page, size);

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), content.size());

        List<T> pageContent = content.subList(start, end);
        return new PageImpl<>(pageContent, pageRequest, content.size());
    }

    private Pageable createPageRequestUsing(int page, int size) {
        return PageRequest.of(page, size);
    }
}
