package com.yzw.base.util.serial;
abstract class SerialNumber {

    public synchronized String getSerialNumber() {
        return process();
    }
    protected abstract String process();
}