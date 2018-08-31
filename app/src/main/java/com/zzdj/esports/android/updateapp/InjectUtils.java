package com.zzdj.esports.android.updateapp;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Field;

public class InjectUtils {
    public static void inject(Activity activity){//定义为静态方法,可以直接类名.调用
        Class clazz = activity.getClass();  //先获取activity的字节码对象
        Class mViewClass = View.class;      //获取View的字节码对象,用来判断我们获取的成员变量是不是View对象
        Class type = null;                  //定义一个Class类型的type引用,用来表示获取到的字段的类型
        Field[] fields = clazz.getDeclaredFields(); //获取activity对象中的所有字段的引用
        for (Field field : fields) {                //遍历数组
            /**
             * 1.设置字段为可以访问
             * 如果不设置,在获取私有字段的时候,就会抛出异常
             * java.lang.IllegalAccessException: access to field not allowed
             */
            field.setAccessible(true);          //设置字段为可以访问(ps,如果不设置,而该字段对象是private,就会导致抛异常)
            //2.获取字段的类型
            type = field.getType();             //获取字段的字节码对象
            /**
             * 3.判断该字段是否为View的子类
             * 返回值为true,表示mViewClass与type表示的类和接口相同,或者mViewClass是type的父类
             */
            if (!mViewClass.isAssignableFrom(type)) {   //判断该字段的类型是不是View或是View的子类
                System.out.println(field + "不是View的子类");
                continue;
            }
            /**
             * 判断该字段是否有注解,flag为true表示添加了注解,false表示没有添加注解
             * present,提出,介绍,呈现
             */
            boolean flag = field.isAnnotationPresent(BindView.class);//判断字段是否有ViewId的注解
            if (!flag) {
                continue;
            }
            /**
             * 获取注解对象,如果注解对象为null,则结束本次循环
             */
            BindView viewId = field.getAnnotation(BindView.class);  //获取field对应的注解对象
            if (viewId == null) {
                continue;
            }
            /**
             * 获取注解表示的id
             */
            int valueId = viewId.id();       //获取注解对象的值
            try {
                View view = activity.findViewById(valueId); //拿到值后,调用activity中的findViewById,获取View对象

                //如果findViewById查找的结果是null,说明id不存在或用户恶意攻击,直接甩出一个异常
                if (view == null) {
                    throw new IllegalStateException("there is a null result when findViewById for " + field);
                }
                field.set(activity, /*type.cast(view)*/view);//设置字段field的值, type.cast(view),表示将view强转为type类型
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
