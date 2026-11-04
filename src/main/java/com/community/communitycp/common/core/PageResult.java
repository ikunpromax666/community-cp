package com.community.communitycp.common.core;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

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
     * <p>
     * 仅当 records 元素类型与目标一致时可用；
     * Entity -> VO 的跨类型转换请在 Service 里手动 new PageResult + 手动 setRecords。
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
}
