package com.bcs.core.json;

import com.bcs.core.utils.ObjectUtil;

import java.io.Serializable;

/**
 * @author ???
 */
public abstract class AbstractBcsEntity implements Serializable, Cloneable {
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

    public String toJson(){
        return ObjectUtil.objectToJsonStr(this);
    }
}
