package com.xiao.embeddedcar.Entity;

import androidx.annotation.NonNull;

public class LoginInfo {
    //设备IP
    private String IP;
    //摄像头IP
    private String IPCamera = null;
    private String pureCameraIP = null;

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getIPCamera() {
        return IPCamera;
    }

    public void setIPCamera(String IPCamera) {
        this.IPCamera = IPCamera;
    }

    public String getPureCameraIP() {
        return pureCameraIP;
    }

    public void setPureCameraIP(String pureCameraIP) {
        this.pureCameraIP = pureCameraIP;
    }

    @NonNull
    @Override
    public String toString() {
        return "LoginInfo{" +
                "IP='" + IP + '\'' +
                ", IPCamera='" + IPCamera + '\'' +
                ", pureCameraIP='" + pureCameraIP + '\'' +
                '}' + "\n";
    }
}
