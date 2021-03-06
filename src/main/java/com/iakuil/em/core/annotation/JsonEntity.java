package com.iakuil.em.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将JavaBean标注为JSON实体字段
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface JsonEntity {
}
