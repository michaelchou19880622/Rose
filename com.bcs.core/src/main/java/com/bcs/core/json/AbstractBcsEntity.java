package com.bcs.core.json;

import java.io.Serializable;

import com.bcs.core.utils.ObjectUtil;

public abstract class AbstractBcsEntity implements Serializable, Cloneable  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
	   return ObjectUtil.objectToJsonStr(this);
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
