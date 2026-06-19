package com.zhihui.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageVO<T> {
    private int page;        // 当前页码
    private int size;        // 每页条数
    private long total;      // 总记录数
    private int totalPages;  // 总页数
    private List<T> records; // 当前页数据

    /**
     * 简化构造方法：自动计算总页数
     */
    public PageVO(int page, int size, long total, List<T> records) {
        this.page = page;
        this.size = size;
        this.total = total;
        // 向上取整计算总页数：总条数/每页大小，比如101条，每页10条 → 11页
        this.totalPages = (int) Math.ceil((double) total / size);
        this.records = records;
    }
}

