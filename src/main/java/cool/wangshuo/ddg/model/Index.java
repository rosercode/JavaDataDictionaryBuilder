package cool.wangshuo.ddg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wangshuo
 * @description 数据库表索引实体类
 * @createDate 2022/11/10 12:39
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Index {

    /**
     * 索引名称
     */
    private String name;
    /**
     * 是否唯一索引
     */
    private boolean unique;
    /**
     * 索引列 逗号分隔
     */
    private String fields;


    public String getName() {
        return name;
    }

    public Index setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isUnique() {
        return unique;
    }

    public Index setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }

    public String getFields() {
        return fields;
    }

    public Index setFields(String fields) {
        this.fields = fields;
        return this;
    }


    @Override
    public String toString() {
        return "Index{" +
                ", name='" + name + '\'' +
                ", unique=" + unique +
                ", fields='" + fields + '\'' +
                '}';
    }
}