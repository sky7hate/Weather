package com.way.bean;

import com.google.gson.annotations.Expose;

/**
 * Created by Totti on 2015/12/7.
 */
public class F1 {
    @Expose
    String fa, fb, fc, fd, fe, ff, fg, fh, fi;
    public String getfa() {
        return fa;
    }
    public void setfa(String fa) {
        this.fa = fa;
    }
    public String getfb() {
        return fb;
    }
    public void setfb(String fb) {
        this.fb = fb;
    }
    public String getfc() {
        return fc;
    }
    public void setfc(String fc) {
        this.fc = fc;
    }
    public String getfd() {
        return fd;
    }
    public void setfd(String fd) {
        this.fd = fd;
    }
    public String getfe() {
        return fe;
    }
    public void setfe(String fe) {
        this.fe = fe;
    }
    public String getff() {
        return ff;
    }
    public void setff(String ff) {
        this.ff = ff;
    }
    public String getfg() {
        return fg;
    }
    public void setfg(String fg) {
        this.fg = fg;
    }
    public String getfh() {
        return fh;
    }
    public void setfh(String fh) {
        this.fh = fh;
    }
    public String getfi() {
        return fi;
    }
    public void setfi(String fi) {
        this.fi = fi;
    }
    @Override
    public String toString() {		return "Weather [" + fa + " "+ fb + " "+ fc+ " " + fd+ " " + fe+ " " + ff+ " " + fg+ " " + fh+ " " + fi + "]";	}
}
