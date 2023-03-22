package com.company.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtils
{
    public static Pageable getPageable(int page, int size, String sortOrder, boolean ascending)
    {
        Sort sort = getSortOption(sortOrder, ascending);
        return PageRequest.of(page, size, sort);
    }

    private static Sort getSortOption(String sortOrder, boolean ascending)
    {
        if(ascending)
        {
            return Sort.by(sortOrder).ascending();
        }

        return Sort.by(sortOrder).descending();
    }
}
