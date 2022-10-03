package com.wang.hbase;

import com.wang.hbase.domain.StudentCell;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/*
Hbase shell相关命令：
    创建命名空间: create_namespace 'bigdata'

    查看所有的命名空间: list_namespace

    创建表: 有如下两种方式：
        create 'student1','info' （默认放到default空间中）
        create 'bigdata:student', {NAME => 'info', VERSIONS =>5}, {NAME => 'msg'} （放到bigdata空间）

    查看所有的表名：list

    查看一个表的详情: describe 'student1'

    修改表：主要分为如下两种情况：
        增加列族和修改信息都使用覆盖的方法: alter 'student1', {NAME => 'f1', VERSIONS => 3}
        删除信息使用特殊的语法，例如下面两种方式：
            alter 'student1', NAME => 'f1', METHOD => 'delete'
            alter 'student1', 'delete' => 'f1'

    删除表: 需要先将表格状态设置为不可用，然后再删除，实现下面两个指令：
        disable 'student1'
        drop 'student1'

    写入数据：put 'bigdata:student','1001','info:name','lisi'

    读取数据：
        get 'bigdata:student','1001'
        get 'bigdata:student','1001' , {COLUMN => 'info:name'}

    扫描数据：scan 'bigdata:student',{STARTROW => '1001',STOPROW =>'1002'}

    删除数据：执行命令会标记数据为要删除，不会直接将数据彻底删除，删除数据只在特定时期清理磁盘时进行
        删除一个版本：delete 'bigdata:student','1001','info:name'
        删除所有版本： deleteall 'bigdata:student','1001','info:name'


 */

public class HbaseDdlAndDmlSample {
    public Connection connection;

    public HbaseDdlAndDmlSample() {
        connection = HbaseConnection.getConnection();
    }

    /**
     * 创建命名空间
     */
    public void createNamespace(String namespace) throws Exception {
        // admin是轻量级的，不是线程安全的，不推荐池化或者缓存这个连接
        Admin admin = connection.getAdmin();
        NamespaceDescriptor namespaceDescriptor = NamespaceDescriptor.create(namespace).build();
        admin.createNamespace(namespaceDescriptor);
        admin.close();
    }

    /**
     * 获取所有命名空间名称
     */
    public List<String> listNamespace() throws Exception {
        Admin admin = connection.getAdmin();
        String[] namespaces = admin.listNamespaces();
        admin.close();
        return Arrays.stream(namespaces).collect(Collectors.toList());
    }

    /**
     * 表是否存在
     */
    public boolean tableExists(String namespace, String tableName) throws Exception {
        Admin admin = connection.getAdmin();
        boolean hasTable = admin.tableExists(TableName.valueOf(namespace, tableName));
        admin.close();
        return hasTable;
    }

    /**
     * 创建表
     */
    public void createTable(String namespace, String tableName, List<String> columnFamilies) throws Exception {
        Admin admin = connection.getAdmin();
        TableName table = TableName.valueOf(namespace, tableName);
        if (admin.tableExists(table)) {
            System.out.println("table already exist...");
            return;
        }

        List<ColumnFamilyDescriptor> columnFamilyDescriptors = columnFamilies.stream().map(columnFamily -> ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(columnFamily)).build()).collect(Collectors.toList());
        TableDescriptor tableDescriptor = TableDescriptorBuilder.newBuilder(table)
                .setColumnFamilies(columnFamilyDescriptors)
                .build();
        admin.createTable(tableDescriptor);
        admin.close();
    }


    /**
     * 修改表中一个列族保存的最大版本数
     */
    public void modifyTableColumnFamilyVersion(String namespace, String tableName, String columnFamily, int maxVersion) throws Exception {
        Admin admin = connection.getAdmin();
        TableName table = TableName.valueOf(namespace, tableName);
        // 需要现获取旧的表描述对象，再修改
        TableDescriptor tableDescriptor = admin.getDescriptor(table);
        // 修改需要通过builder对象来进行，TableDescriptor中没有修改的方法
        TableDescriptorBuilder tableDescriptorBuilder = TableDescriptorBuilder.newBuilder(tableDescriptor);

        // 需要现获取旧的表列族描述对象，再修改
        ColumnFamilyDescriptor columnFamilyDescriptor = tableDescriptor.getColumnFamily(Bytes.toBytes(columnFamily));
        System.out.println("old family version ---> " + columnFamilyDescriptor.getMaxVersions());
        // 修改需要通过builder对象来进行，ColumnFamilyDescriptor中没有修改的方法
        ColumnFamilyDescriptorBuilder columnFamilyDescriptorBuilder = ColumnFamilyDescriptorBuilder.newBuilder(columnFamilyDescriptor);
        // 修改版本号
        columnFamilyDescriptorBuilder.setMaxVersions(maxVersion);
        tableDescriptorBuilder.modifyColumnFamily(columnFamilyDescriptorBuilder.build());

        // 修改表
        admin.modifyTable(tableDescriptorBuilder.build());
        admin.close();
    }

    /**
     * 删除表
     */
    public void deleteTable(String namespace, String tableName) throws Exception {
        Admin admin = connection.getAdmin();
        TableName table = TableName.valueOf(namespace, tableName);
        if (!admin.tableExists(table)) {
            System.out.println("table not exist...");
            return;
        }

        // 需要先disable，再删除表格
        if (!admin.isTableDisabled(table)) {
            admin.disableTable(table);
        }
        admin.deleteTable(table);
        admin.close();
    }

    /**
     * 向表中插入某列的数据
     * @param namespace 命名空间名称
     * @param tableName 表名
     * @param rowKey 主键
     * @param columnFamily 列族名称
     * @param columnName 列明
     * @param value 插入的值
     * @throws Exception
     */
    public void putCell(String namespace, String tableName, String rowKey, String columnFamily, String columnName, String value) throws Exception {
        // table是轻量级的，不是线程安全的，不推荐池化或者缓存这个连接
        Table table = connection.getTable(TableName.valueOf(namespace, tableName));

        // 构建数据对象
        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName), Bytes.toBytes(value));
        // 插入数据
        table.put(put);

        table.close();
    }

    /**
     * 读取表中某一列的数据
     */
    public String getCellValue(String namespace, String tableName, String rowKey, String columnFamily, String columnName) throws Exception {
        Table table = connection.getTable(TableName.valueOf(namespace, tableName));

        // 创建get对象
        Get get = new Get(Bytes.toBytes(rowKey));
        // 如果什么都不加，读取整个行数据
        // 读取某个列族数据
//        get.addFamily()
        // 读取某个列族的某个列数据
        get.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName));

        // 设置读取的版本，一般都是读取最新版本，之前版本意义不大
        get.readAllVersions();

        Result result = table.get(get);
        List<String> values = Arrays.stream(result.rawCells())
                // 转换成正常字符串，否则是乱码的
                .map(cell -> CellUtil.cloneValue(cell))
                .map(bytes -> new String(bytes)).collect(Collectors.toList());

        table.close();
        return CollectionUtils.isEmpty(values) ? null : values.get(0);
    }

    /**
     * 扫描数据
     */
    public List<StudentCell> scanRows(String namespace, String tableName, String startRow, String stopRow) throws Exception {
        Table table = connection.getTable(TableName.valueOf(namespace, tableName));

        Scan scan = new Scan();
        // 如果不添加条件，会直接扫描整个表
        // 默认包含
        scan.withStartRow(Bytes.toBytes(startRow));
//        scan.withStartRow(Bytes.toBytes(startRow), true);
        // 默认不包含
        scan.withStopRow(Bytes.toBytes(stopRow));
//        scan.withStopRow(Bytes.toBytes(stopRow), false);
        // 也可以限制一些列族，列等信息
//        scan.addFamily()
//        scan.addColumn()
        // 可以通过filter限制，列入列大于多少等，可以多个条件
//        FilterList filterList = new FilterList();
//        // 列值过滤器，如果某行数据没有这个列名，也会保留下来
//        ColumnValueFilter columnValueFilter = new ColumnValueFilter(Bytes.toBytes("info"), Bytes.toBytes("name"), CompareOperator.EQUAL, Bytes.toBytes("zhangsan"));
//        filterList.addFilter(columnValueFilter);
//        scan.setFilter(filterList);

        List<StudentCell> studentCells = new ArrayList<>();
        ResultScanner scanner = table.getScanner(scan);
        for (Result result : scanner) {
            for (Cell cell : result.rawCells()) {
                StudentCell studentCell = new StudentCell();
                studentCell.setRowKey(new String(CellUtil.cloneRow(cell)));
                studentCell.setFamilyName(new String(CellUtil.cloneFamily(cell)));
                studentCell.setColumnName(new String(CellUtil.cloneQualifier(cell)));
                studentCell.setValue(new String(CellUtil.cloneValue(cell)));
                studentCells.add(studentCell);
            }
        }

        table.close();
        return studentCells;
    }

    /**
     * 删除某个列数据
     */
    public void deleteColumn(String namespace, String tableName, String rowKey, String columnFamily, String columnName) throws Exception {
        Table table = connection.getTable(TableName.valueOf(namespace, tableName));

        Delete delete = new Delete(Bytes.toBytes(rowKey));
        // 删除一个版本数据，回滚数据可以考虑使用这个
//        delete.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName));
        // 删除所有版本数据， 按照逻辑，一般需要删除所有版本数据
        delete.addColumns(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName));
        table.delete(delete);

        table.close();
    }

}
