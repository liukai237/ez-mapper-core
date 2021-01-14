package com.iakuil.em.core;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Id;
import java.io.Serializable;

/**
 * Entity基类
 * <p>【强制】所有Table必须设计ID字段。</p>
 *
 * @author Kai
 */
public class BaseEntity implements Serializable {

    @Id
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
