package ca.vanzyl.provisio.tools.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.immutables.value.Value;

// Note that adding jdkOnly = true to the options documented here http://immutables.github.io/style.html doesn't work.
// something in that documented exampled must conflict with jdkOnly = true

@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@Value.Style(
    jdkOnly = true
)
public @interface ImmutablesStyle {}