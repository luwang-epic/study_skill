package com.wang.hbase;

import com.wang.hbase.domain.StudentCell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class HbaseDdlAndDmlSampleTest {

    private HbaseDdlAndDmlSample hbaseDdlAndDmlSample;

    @BeforeEach
    public void before() {
        hbaseDdlAndDmlSample = new HbaseDdlAndDmlSample();
    }

    @Test
    public void testNamespace() throws Exception {
        String namespace = "test";
        hbaseDdlAndDmlSample.createNamespace(namespace);
        List<String> namespaces = hbaseDdlAndDmlSample.listNamespace();
        System.out.println("namespaces ---> " + namespaces);
    }

    @Test
    public void testCreateTable() throws Exception {
        String namespace = "test";
        String tableName = "student";
        List<String> columnFamilies = Arrays.asList("info", "address");
        hbaseDdlAndDmlSample.createTable(namespace, tableName, columnFamilies);
    }

    @Test
    public void testDeleteTable() throws Exception {
        String namespace = "test";
        String tableName = "student";
        hbaseDdlAndDmlSample.deleteTable(namespace, tableName);
    }

    @Test
    public void testModifyTableColumnFamilyVersion() throws Exception {
        String namespace = "test";
        String tableName = "student";
        String columnFamily ="info";
        int version = 5;
        hbaseDdlAndDmlSample.modifyTableColumnFamilyVersion(namespace, tableName, columnFamily, version);
    }

    @Test
    public void testPutCell() throws Exception {
        String namespace = "test";
        String tableName = "student";
        String rowKey = "2";
        String columnFamily ="info";
        String columnName = "name";
        String value = "lisi";
        hbaseDdlAndDmlSample.putCell(namespace, tableName, rowKey, columnFamily, columnName, value);
    }

    @Test
    public void testGetCell() throws Exception {
        String namespace = "test";
        String tableName = "student";
        String rowKey = "1";
        String columnFamily ="info";
        String columnName = "name";
        String value = hbaseDdlAndDmlSample.getCellValue(namespace, tableName, rowKey, columnFamily, columnName);
        System.out.println("cell value ---> " + value);
    }

    @Test
    public void testScanCell() throws Exception {
        String namespace = "test";
        String tableName = "student";
        String startRowKey = "1";
        String stopRowKey = "3";
        List<StudentCell> studentCells = hbaseDdlAndDmlSample.scanRows(namespace, tableName, startRowKey, stopRowKey);
        System.out.println(studentCells);
    }

    @Test
    public void testDeleteColumn() throws Exception {
        String namespace = "test";
        String tableName = "student";
        String rowKey = "2";
        String columnFamily ="info";
        String columnName = "name";
        hbaseDdlAndDmlSample.deleteColumn(namespace, tableName, rowKey, columnFamily, columnName);
    }

}
