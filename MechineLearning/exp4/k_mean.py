import numpy as np

class KMeans:
    def __init__(self, n_clusters=3, max_iters=300):
        """
        初始化KMeans类
        
        参数:
            n_clusters: 聚类数量
            max_iters: 最大迭代次数
        """
        self.n_clusters = n_clusters
        self.max_iters = max_iters
        self.centroids = None
        self.labels = None
        
    def fit(self, X):
        """
        训练KMeans模型
        
        参数:
            X: 训练数据, shape=(n_samples, n_features)
        """
        # 从数据点中随机选择k个作为初始聚类中心
        n_samples, n_features = X.shape
        idx = np.random.choice(n_samples, self.n_clusters, replace=False)
        self.centroids = X[idx]
        
        for _ in range(self.max_iters):
            # 保存旧的聚类中心
            old_centroids = self.centroids.copy()
            
            # 计算每个样本到各个聚类中心的距离
            distances = np.sqrt(((X - self.centroids[:, np.newaxis])**2).sum(axis=2))
            
            # 将每个样本分配给最近的聚类中心
            self.labels = np.argmin(distances, axis=0)
            
            # 更新聚类中心
            for i in range(self.n_clusters):
                cluster_points = X[self.labels == i]
                if len(cluster_points) > 0:
                    self.centroids[i] = cluster_points.mean(axis=0)
                    
            # 检查是否收敛
            if np.all(old_centroids == self.centroids):
                break
                
        return self
    
    def predict(self, X):
        """
        对新数据进行预测
        
        参数:
            X: 需要预测的数据, shape=(n_samples, n_features)
        返回:
            预测的聚类标签
        """
        distances = np.sqrt(((X - self.centroids[:, np.newaxis])**2).sum(axis=2))
        return np.argmin(distances, axis=0)