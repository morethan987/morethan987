import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.metrics import silhouette_score
from k_mean import KMeans
import matplotlib.pyplot as plt

# 读取鸢尾花数据集
f = pd.read_csv(r'exp4/data/iris.csv')
X = f.iloc[:,1:5].values                             # 属性值,numpy数组（跳过Unnamed: 0列）
y = f.iloc[:,5].values                               # 标签值,numpy数组（Species列）
feature_names = f.columns.tolist()[1:5]              # 特征的列表（跳过Unnamed: 0列）

def run_kmeans(X, k, random_seed):
    """运行KMeans算法并返回结果"""
    np.random.seed(random_seed)
    kmeans = KMeans(n_clusters=k)
    kmeans.fit(X)
    labels = kmeans.predict(X)
    score = silhouette_score(X, labels)
    return kmeans, labels, score

def analyze_cluster_sizes(labels, k):
    """分析每个簇的大小"""
    sizes = []
    for i in range(k):
        size = np.sum(labels == i)
        sizes.append(size)
    return sizes

# 设置不同的k值和随机种子
k_values = [2, 3, 4]
random_seeds = [42, 123, 256]

# 存储所有结果
results = []

# 运行不同参数组合的实验
print("\n=== 聚类实验结果分析 ===")
for k in k_values:
    print(f"\nk = {k} 的实验结果：")
    print("-" * 40)
    for seed in random_seeds:
        kmeans, labels, score = run_kmeans(X, k, seed)
        cluster_sizes = analyze_cluster_sizes(labels, k)
        results.append({
            'k': k,
            'seed': seed,
            'kmeans': kmeans,
            'labels': labels,
            'score': score,
            'cluster_sizes': cluster_sizes
        })
        print(f"随机种子 = {seed}:")
        print(f"- 轮廓系数: {score:.4f}")
        print(f"- 各簇大小: {cluster_sizes}")

# 找出每个k值的最佳结果
print("\n=== 最佳结果分析 ===")
best_results = {}
for k in k_values:
    k_results = [r for r in results if r['k'] == k]
    best_k_result = max(k_results, key=lambda x: x['score'])
    best_results[k] = best_k_result
    print(f"\nk = {k} 的最佳结果：")
    print("-" * 40)
    print(f"- 随机种子: {best_k_result['seed']}")
    print(f"- 轮廓系数: {best_k_result['score']:.4f}")
    print(f"- 各簇大小: {best_k_result['cluster_sizes']}")

# 让图像显示中文
plt.rcParams['font.sans-serif'] = ['SimHei']
plt.rcParams['axes.unicode_minus'] = False

# 选择两个最具代表性的特征
feature_pair = (0, 1)  # sepal length 和 sepal width

# 创建一个大图，展示所有k值的结果
plt.figure(figsize=(15, 5))

for idx, k in enumerate(k_values, 1):
    plt.subplot(1, 3, idx)
    
    result = best_results[k]
    
    # 绘制数据点
    scatter = plt.scatter(X[:, feature_pair[0]], X[:, feature_pair[1]], 
                         c=result['labels'], cmap='viridis', alpha=0.6)
    
    # 绘制聚类中心
    plt.scatter(result['kmeans'].centroids[:, feature_pair[0]], 
                result['kmeans'].centroids[:, feature_pair[1]],
                marker='*', s=100, c='red', label='聚类中心')
    
    plt.xlabel(feature_names[feature_pair[0]])
    plt.ylabel(feature_names[feature_pair[1]])
    plt.title(f'k={k}, 轮廓系数={result["score"]:.4f}\n簇大小={", ".join(str(size) for size in result["cluster_sizes"])}')
    plt.legend()
    
    # 添加颜色条
    plt.colorbar(scatter)

plt.suptitle('不同k值的聚类结果比较', fontsize=12)
plt.tight_layout()
plt.show()

# 绘制轮廓系数随机种子的变化
plt.figure(figsize=(10, 6))
for k in k_values:
    k_results = [r for r in results if r['k'] == k]
    scores = [r['score'] for r in k_results]
    plt.plot(random_seeds, scores, 'o-', label=f'k={k}')

plt.xlabel('随机种子')
plt.ylabel('轮廓系数')
plt.title('不同k值和随机种子的轮廓系数比较')
plt.legend()
plt.grid(True)
plt.show()
