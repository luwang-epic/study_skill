package com.wang.io.nio;

/*
 FileChannel 的优点包括：
    在文件的特定位置读取和写入；
    将文件的一部分直接加载到内存中，这样效率最高；
    可以以更快的速度将文件数据从一个通道传输到另一个通道；
    可以锁定文件的一部分以限制其他线程访问；
    为了避免数据丢失，我们可以强制将更新的文件立即写入存储。
 */

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * nio的文件通道，通过FileChannel操作文件
 */
public class NioFileChannelSample {

    public static void main(String[] args) throws Exception {
        NioFileChannelSample sample = new NioFileChannelSample();
        String readPath = "D:\\idea_project\\study_skill\\file\\java\\nio_read_file.txt";
        String content = sample.readFromFile(readPath);
        System.out.println("file content ---> " + content);

        String writePath = "D:\\idea_project\\study_skill\\file\\java\\nio_write_file.txt";
        sample.writeToFile(writePath, content);
    }


    public String readFromFile(String path) throws Exception {
        /*
        文件模式有如下几种：
            “r”: 表示通道仅开发“读取”，
            “rw”: 对文件的内容的每次更新，并且在你关闭文件之前不会更改修改日期。
            “rws”: 对文件的内容和文件的修改日期（元数据）的每一次更新都同步写入到底层存储设备中；
            “rwd”: 对文件的内容的每一次更新都同步写入底层存储器中，但在文件关闭之前修改日期（元数据）可能不会更改。
         模式“rwd” 的写入速度比“rw” 慢得多，而 “rws” 又更慢得多。使用“rwd”可用于减少执行的I/O操作的数量，
         使用“rws”需要更新文件内容及其要写入的元数据，这起码要进行一次低级I/O操作
         */
        RandomAccessFile reader = new RandomAccessFile(path, "r");
        FileChannel channel = reader.getChannel();

//        FileChannel channel = FileChannel.open(Paths.get(path), StandardOpenOption.READ);

//        FileInputStream fin= new FileInputStream(path);
//        FileChannel channel = fin.getChannel();

        ByteBuffer buf = ByteBuffer.allocate(1024);
        StringBuilder sb = new StringBuilder();
        while (channel.read(buf) != -1) {
            // 缓存区切换到读模式，即将position置为0，limit置为原来的position值
            buf.flip();
            while (buf.hasRemaining()) {
                sb.append((char) buf.get());
            }
            // 清空 buffer，缓存区切换到写模式，即将position置为0，limit置为capacity的值
            // buffer中的数据并没有清除，只是告诉我们从哪里开始往buffer中写数据
            buf.clear();
            // 会将所有没有读取的数据拷贝到buffer起始处，然后将position设到最后一个未读元素后面。
            // limit设置为capacity的值，这样写入数据就不会覆盖未读取的数据了。
//            buf.compact();
        }
        channel.close();
        return sb.toString();
    }

    public void writeToFile(String path, String content) throws Exception {
        FileChannel channel = FileChannel.open(Paths.get(path), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.clear();
        buf.put(content.getBytes());
        buf.flip();
        while (buf.hasRemaining()) {
            channel.write(buf);
        }
        //  操作系统可能出于性能原因缓存文件更改，如果系统崩溃，数据可能会丢失。
        //  要强制文件内容和元数据连续写入磁盘，我们可以使用 force 方法
        //  出于性能方面的考虑，操作系统会将数据缓存在内存中，
        //  所以无法保证写入到 FileChannel 里的数据一定会即时写到磁盘上。
        channel.force(false);
        channel.close();
    }

}
