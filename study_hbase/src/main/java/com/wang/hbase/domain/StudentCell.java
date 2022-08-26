package com.wang.hbase.domain;

import lombok.Data;

@Data
public class StudentCell {
    private String rowKey;
    private String familyName;
    // qualifier name
    private String columnName;
    private String value;
}
