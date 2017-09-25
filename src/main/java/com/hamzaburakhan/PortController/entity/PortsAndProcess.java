package com.hamzaburakhan.PortController.entity;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by hamza on 25.09.2017.
 */
public class PortsAndProcess {
    private String protocol;
    private String localAddress;
    private String foreignAddress;
    private String state;
    private String PID;
    private String proccessName;
    private String port;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        String[] array =localAddress.split(":");
        try {
            this.port = array[array.length-1];

        }catch (Exception e){
            e.printStackTrace();
        }
        this.localAddress = localAddress;
    }

    public String getForeignAddress() {
        return foreignAddress;
    }

    public void setForeignAddress(String foreignAddress) {
        this.foreignAddress = foreignAddress;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPID() {
        return PID;
    }

    public void setPID(String PID)  {

        this.PID = PID;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getProccessName() {
        return proccessName;
    }

    public void setProccessName(String proccessName) {
        this.proccessName = proccessName;
    }

    @Override
    public String toString() {
        return  getProccessName()+"\t"+getProtocol()+"\t"+getLocalAddress()+"\t"+getForeignAddress()+"\t"+getState()+"\t"+getPID()+"\n";
    }
}
