package channel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: xck
 * @File: ChannelPipelineCoverage
 * @Time: 14:30 2019/8/16
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChannelPipelineCoverage {
    String ALL = "all";
    String ONE = "one";

    String value();
}
