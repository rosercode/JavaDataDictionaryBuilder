package cool.wangshuo.ddg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wangshuo
 * @description 数据库配置实体类
 * @createDate 2022/11/10 12:40
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DbConfig {

    private String driver ;
    private String url;
    private String username;
    private String password;

}
