package com.jiewu.driftbottle.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageRequest implements Serializable {
    private static final long serialVersionUID = 7749826257893L;

    /**
     * 页面大小
     */
    private Integer pageSize = 10;

    /**
     * 当前页码
     */
    private Integer pageNum = 1;
}
