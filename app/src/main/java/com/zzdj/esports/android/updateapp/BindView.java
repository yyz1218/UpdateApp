package com.zzdj.esports.android.updateapp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)//表示要注解的是一个字段
@Retention(RetentionPolicy.RUNTIME)//运行时
public @interface BindView {
    //view的id
    public int id();
    //是否可点击,默认为false
    public boolean clickable() default  true;
}
