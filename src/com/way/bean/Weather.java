package com.way.bean;

import com.google.gson.annotations.Expose;

public class Weather {
	@Expose
	private C c;
	private F f;

	public F getf() {
		return f;
	}
	public void setf(F f) {
		this.f = f;
	}
	public C getc() {
		return c;
	}
	public void setc(C c) {
		this.c = c;
	}

	//@Override
	//public String toString() {		return "Weather [weatherinfo=" + weatherinfo + "]";	}

}
