package com.application.steammachine.utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TableInfo{



    private final String info;
    private final String value;

    public TableInfo(String info, String value) {
        this.info = info;
        this.value = value;
    }

    public String getInfo() {
        return info;
    }

    public String getValue() {
        return value;
    }


}