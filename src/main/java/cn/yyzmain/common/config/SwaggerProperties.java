
package cn.yyzmain.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * swagger-ui的api信息
 *
 */
@ConfigurationProperties(prefix = "swagger.api-info")
@Data
public class SwaggerProperties {

    private String title = "main restful apis";
    private String description = "main-study";
    private String version = "1.0";

    private String basePackage = "cn.yyzmain";

}
