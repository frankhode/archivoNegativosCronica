package com.archivonegativoscronica;


public class ColumnaConfig {

    private final String header;
    private final String property;
    private final double width;

    public ColumnaConfig(String header, String property, double width) {
        this.header = header;
        this.property = property;
        this.width = width;
    }

    public String getHeader()   { return header; }
    public String getProperty() { return property; }
    public double getWidth()    { return width; }
}
