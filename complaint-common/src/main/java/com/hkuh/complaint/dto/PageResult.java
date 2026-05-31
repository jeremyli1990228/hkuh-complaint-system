package com.hkuh.complaint.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> list;

    private long total;

    private int pageNum;

    private int pageSize;

    private int pages;

    public PageResult(List<T> list, long total, int pageNum, int pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) Math.ceil((double) total / pageSize);
    }

    public static <T> PageResult<T> of(List<T> list, long total, int pageNum, int pageSize) {
        return new PageResult<>(list, total, pageNum, pageSize);
    }

    public boolean hasNext() {
        return this.pageNum < this.pages;
    }

    public boolean hasPrevious() {
        return this.pageNum > 1;
    }

    public boolean isFirst() {
        return this.pageNum == 1;
    }

    public boolean isLast() {
        return this.pageNum >= this.pages;
    }
}
