package com.claw.common.base;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 实体父类
 */
@Getter
@Setter
public abstract class BaseEntity implements Serializable {

	private static final long serialVersionUID = -7176390653391227433L;

}
