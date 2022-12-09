package cool.wangshuo.ddg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wangshuo
 * @description 数据库表实体类
 * @createDate 2022/11/10 12:40
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Table {

    /**
     * 表名称
     */
    private String name;
    /**
     * 注释
     */
    private String remarks;

    /**
     * 主键字段
     */
    private String primaryKey;

    /**
     * 索引列表
     */
    private List<Index> indexList;

    /**
     * 字段列表
     */
    private List<Column> columnList;

    public Table(String name, String remarks) {
        this.name = name;
        this.remarks = remarks;
    }
}
