package com.community.communitycp.common.core;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 分页返回结果封装
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> {

    @Schema(description = "当前页数据")
    private List<T> records;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "总页数")
    private Long pages;

    @Schema(description = "当前页码")
    private Long current;

    @Schema(description = "每页条数")
    private Long size;

    /**
     * 直接由 MyBatis-Plus 的 IPage 转换
     */
    public static <T> PageResult<T> from(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    /**
     * 由 IPage 转换为另一种元素类型（如 Entity -> VO）
     */
    public static <E, T> PageResult<T> from(IPage<E> page, Function<E, T> converter) {
        PageResult<T> result = new PageResult<>();
        if (page.getRecords() == null) {
            result.setRecords(Collections.emptyList());
        } else {
            List<T> records = new ArrayList<>(page.getRecords().size());
            for (E item : page.getRecords()) {
                records.add(converter.apply(item));
            }
            result.setRecords(records);
        }
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }
}
