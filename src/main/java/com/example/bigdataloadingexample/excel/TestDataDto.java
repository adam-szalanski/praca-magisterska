package com.example.bigdataloadingexample.excel;

import lombok.Data;

@Data
public class TestDataDto {

    private String fileSize;
    private double cpuLoadMethodJpa;
    private double ramUsageMethodJpa;
    private long durationMethodJpa;
    private double cpuLoadMethodJdbc;
    private double ramUsageMethodJdbc;
    private long durationMethodJdbc;
    private String testDate;
}
