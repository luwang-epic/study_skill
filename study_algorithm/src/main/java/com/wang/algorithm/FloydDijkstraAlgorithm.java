package com.wang.algorithm;


/**   Floyd和Dijkstra算法   对比： 
 * 1、如果依次对某个顶点运用Dijkstra算法，则与Floyd算法相比，很多路径和结果计算是重复的，
 * 			虽然复杂度相同，但是运算量差了很多.
 * 2、更为重要的是：Dijkstra算法使用的前提是图中路径长度必须大于等于0；
 * 			但是Floyd算法则仅仅要求没有总和小于0的环路就可以了
 * 
 * 因此Floyd 算法应用范围比Dijkstra算法要广。
 */
public class FloydDijkstraAlgorithm {

	private static final int INF = Integer.MAX_VALUE;   // 最大值   表示 此路不通
	
	/*
	 * floyd最短路径。
	 * 即，统计图中各个顶点间的最短路径。
	 *
	 * 参数说明：
	 *     path -- 路径。path[i][j]=k表示，"顶点i"到"顶点j"的最短路径会经过顶点k。
	 *     dist -- 长度数组。即，dist[i][j]=sum表示，"顶点i"到"顶点j"的最短路径的长度是sum。
	 *     weight -- 邻接矩阵
	 */
	public static void floyd(int[][] weight) {	
		//记录最短路径
		int[][] path = new int[weight.length][weight.length];
		//记录最短路径长度
		int[][] dist = new int[weight.length][weight.length];
		
		//初始化
		for(int i=0; i<weight.length; i++){
			for(int j=0; j<weight.length; j++){
				dist[i][j] = weight[i][j]; // "顶点i"到"顶点j"的路径长度为"i到j的权值"
				if(weight[i][j] < INF){
					path[i][j] = j; // "顶点i"到"顶点j"的最短路径是经过顶点j。
				}else
					path[i][j] = -1;
			}
		}
		
		
		//更新，计算最短路径
		for(int k=0; k<weight.length; k++){  //i到j的最短路径是否经过k  这个必须在最外面的循环
			for(int i=0; i<weight.length; i++){
				for(int j=0; j<weight.length; j++){
					 // 如果经过下标为k顶点路径比原两点间路径更短，则更新dist[i][j]和path[i][j]
					if(dist[i][k]!=INF && dist[k][j]!=INF 
							&& dist[i][j] > dist[i][k]+dist[k][j]){
						// "i到j最短路径"对应的路径，经过k
						path[i][j]=path[i][k];
						// "i到j最短路径"对应的值设，为更小的一个(即经过k)
						dist[i][j] = dist[i][k]+dist[k][j];
					}

				}
			}
		}
		
	    // 打印floyd最短路径的结果
	    System.out.printf("floyd: \n");
	    for (int i = 0; i < weight.length; i++) {
	        for (int j = 0; j < weight.length; j++) {
				System.out.printf("%2d  ", dist[i][j]);
			}
	        System.out.printf("\n");
	    }
	    
	    System.out.printf("path: \n");
	    for (int i = 0; i < weight.length; i++) {
	        for (int j = 0; j < weight.length; j++)
	            System.out.printf("%2d  ", path[i][j]);
	        System.out.printf("\n");
	    }
	    
	    System.out.println("+++++++++++++++++++++++++++++++++++");  
	    
	    
	    //打印最短路径
	    for (int i = 0; i < weight.length; i++) {  
	         for (int j = 0; j < weight.length; j++) {  
	             System.out.print("输出i=" + i + "到j=" + j + "最短路径：");
	             System.out.print(i);
	             int k = path[i][j];  
	             if (k == -1) {  
	                 System.out.println("没有最短路径");  
	             } else {  
	                 System.out.print(" " + k);  
	                 while (k != j) {  
	                     k = path[k][j];  
	                     System.out.print(" " + k);  
	                 }  
	                 
	                 System.out.println();  
	             }  
	         }  
	     } 
	    
	}
	
	
	// 注意：该函数会改变邻接矩阵weight的值，因此后面不能再使用该临界矩阵，最好深复制一份邻接矩阵传给该函数
	public static int[] dijkstra(int[][] weight, int start) {
		// 接受一个有向图的权重矩阵，和一个起点编号start（从0编号，顶点存在数组中）
		// 返回一个int[] 数组，表示从start到它的最短路径长度
		int n = weight.length; // 顶点个数
		int[] shortPath = new int[n]; // 保存start到其他各点的最短路径
		String[] path = new String[n]; // 保存start到其他各点最短路径的字符串表示
		for (int i = 0; i < n; i++) {
			path[i] = new String(start + "-->" + i);
		}
		int[] visited = new int[n]; // 标记当前该顶点的最短路径是否已经求出,1表示已求出

		// 初始化，第一个顶点已经求出
		shortPath[start] = 0;
		visited[start] = 1;

		for (int count = 1; count < n; count++) { // 要加入n-1个顶点
			int k = -1; // 选出一个距离初始顶点start最近的未标记顶点
			int dmin = Integer.MAX_VALUE;
			for (int i = 0; i < n; i++) {
				if (visited[i] == 0 && weight[start][i] < dmin) {
					dmin = weight[start][i];
					k = i;
				}
			}

			// 将新选出的顶点标记为已求出最短路径，且到start的最短路径就是dmin
			shortPath[k] = dmin;
			visited[k] = 1;

			// 以k为中间点，修正从start到未访问各点的距离
			for (int i = 0; i < n; i++) {
				if (visited[i] == 0 && weight[start][k] != INF && weight[k][i]!=INF
						&& weight[start][k] + weight[k][i] < weight[start][i]) {
					weight[start][i] = weight[start][k] + weight[k][i];
					path[i] = path[k] + "-->" + i;
				}
			}
		}
		for (int i = 0; i < n; i++) {
			System.out.println("从" + start + "出发到" + i + "的最短路径为：" + path[i]);
		}
		System.out.println("=====================================");
		return shortPath;
	}
	
	
	
	
	public static void main(String[] args) {
		int[][] data = new int[][]{
			{0,12,INF,INF,INF,16,14},
			{12,0,10,INF,INF,7,INF},
			{INF,10,0,3,5,6,INF},
			{INF,INF,3,0,4,INF,INF},
			{INF,INF,5,4,0,2,8},
			{16,7,6,INF,2,0,9},
			{14,INF,INF,INF,8,9,0}
			};
		floyd(data);


		System.out.println("\n\ndijkstra算法------------->\n");
		int start = 0;
		int[] shortPath = dijkstra(data, start);
		for (int i = 0; i < shortPath.length; i++) {
			System.out.println("从" + start + "出发到" + i + "的最短距离为：" + shortPath[i]);
		}
	}

}
