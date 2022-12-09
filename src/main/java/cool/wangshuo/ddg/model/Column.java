package cool.wangshuo.ddg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wangshuo
 * @description 列实体类
 * @createDate 2022/11/10 12:38
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Column {

    /**
     * 字段名
     */
    private String name;
    /**
     * 字段类型
     */
    private String type;
    /**
     * 字段长度
     */
    private int size;
    /**
     * 小数
     */
    private int digits;
    /**
     * 可否为空
     */
    private boolean nullable;
    /**
     * 是否自增
     */
    private boolean autoIncrement;
    /**
     * 默认值
     */
    private String defaultValue;
    /**
     * 备注
     */
    private String remarks;
}