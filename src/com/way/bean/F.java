package com.way.bean;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by Totti on 2015/12/7.
 */
public class F {
    @Expose
    String f0;
    @Expose
    List<F1> f1;
    public String getf0() {
        return f0;
    }
    public void setf0(String f0) {
        this.f0 = f0;
    }
    public List<F1> getf1() {
        return f1;
    }
    public void setf1(List<F1> f1) {
        this.f1 = f1;
    }

}
