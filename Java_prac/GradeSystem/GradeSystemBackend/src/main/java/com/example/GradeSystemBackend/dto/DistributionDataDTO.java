package com.example.GradeSystemBackend.dto;

public class DistributionDataDTO {

    private String range;
    private Integer count;

    public DistributionDataDTO() {}

    public DistributionDataDTO(String range, Integer count) {
        this.range = range;
        this.count = count;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
