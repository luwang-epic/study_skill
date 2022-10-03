package com.wang.jdk.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Lock接口以及对象，使用它，很优雅的控制了竞争资源的安全访问，但是这种锁不区分读写，称这种锁为普通锁。
 * 为了提高性能，Java提供了读写锁，在读的地方使用读锁，在写的地方使用写锁，灵活控制，
 * 如果没有写锁的情况下，读是无阻塞的,在一定程度上提高了程序的执行效率。
 * Java中读写锁有个接口java.util.concurrent.locks.ReadWriteLock，也有具体的实现ReentrantReadWriteLock
 * 
 * ReentrantReadWriteLock 和 ReentrantLock 不是继承关系，但都是基于 AbstractQueuedSynchronizer来实现。
 * 
 * 注意： 在同一线程中，持有读锁后，不能直接调用写锁的lock方法 ，否则会造成死锁。
 *
 */
public class ReentrantReadWriteLockDemo {
	
	
	static class MyCount {
		private String oid; // 账号
		private int cash; // 账户余额

		MyCount(String oid, int cash) {
			this.oid = oid;
			this.cash = cash;
		}

		public String getOid() {
			return oid;
		}

		public void setOid(String oid) {
			this.oid = oid;
		}

		public int getCash() {
			return cash;
		}

		public void setCash(int cash) {
			this.cash = cash;
		}

		@Override
		public String toString() {
			return "MyCount{" + "oid= " + oid  + ", cash=" + cash + '}';
		}
	}
	
	
	static class User implements Runnable{
		private String name;                 //用户名  
        private MyCount myCount;         //所要操作的账户  
        private int iocash;                 //操作的金额，当然有正负之分了  
        
        //读写锁
        private ReadWriteLock lock;
        
        //是否读取金额
        private boolean isCheck;         //是否查询  
        
        User(String name, MyCount myCount, int iocash, ReadWriteLock lock, boolean isCheck) {  
                this.name = name;  
                this.myCount = myCount;  
                this.iocash = iocash;  
                this.lock = lock;  
                this.isCheck = isCheck;  
        } 

		@Override
		public void run() {
			
			if(isCheck){
				//获取读锁
				lock.readLock().lock();
				try {
					System.out.println("读：" + name + "正在查询" + myCount 
											+ "账户，当前金额为" + myCount.getCash());
				} finally {
					//释放读锁
					lock.readLock().unlock();
				}	
			}else{
				
				try {
					
					//获得写锁
					lock.writeLock().lock();
					System.out.println("写：" + name + "正在操作" + myCount + "账户，金额为" + iocash +"，当前金额为" + myCount.getCash());
					
					//写入账号
					myCount.setCash(myCount.getCash()+ iocash);
					System.out.println("写：" + name + "操作" + myCount + "账户成功，金额为" + iocash +"，当前金额为" + myCount.getCash()); 
						
				} finally {
					//释放写锁
					lock.writeLock().unlock();
				}
				
				
			}
			
			
		}
		
	}
	
	

	public static void main(String[] args) {
		
		//创建并发访问的账户  
        MyCount myCount = new MyCount("95599200901215522", 10000);  
        //创建一个锁对象  
        ReadWriteLock lock = new ReentrantReadWriteLock(false);
        
        //创建2个线程的固定大小的线程池
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
		// 创建一些并发访问用户，一个信用卡，存的存，取的取，好热闹啊
		User u1 = new User("张三", myCount, -4000, lock, false);
		User u2 = new User("李四", myCount, 6000, lock, false);
		User u3 = new User("张三", myCount, 800, lock, false);
		User u4 = new User("李四", myCount, 0, lock, true);
		
		// 在线程池中执行各个用户的操作
		executor.execute(u1);
		executor.execute(u2);
		executor.execute(u3);
		executor.execute(u4);
		
		
		// 关闭线程池
		executor.shutdown();
	
		
	}

}
