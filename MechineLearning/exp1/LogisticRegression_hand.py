import numpy as np
import pandas as pd
from matplotlib import pyplot as plt
from sklearn.metrics import accuracy_score, confusion_matrix, classification_report
import seaborn as sns

#导入数据，处理数据
data = pd.read_csv('./data/3.0.csv', encoding='utf-8')
#数据有非数值属性，我们需要对其进行编码,这里使用one_hot 编码，以避免奇怪的数值关系
categorical_columns = ['色泽', '根蒂', '敲声', '纹理', '脐部', '触感']
data = pd.get_dummies(data, columns=categorical_columns)
#目标变量编码
data['好瓜'] = data['好瓜'].replace({'是': 1, '否': 0})
#选择特征和目标变量
X = data.drop(columns=['好瓜', '编号']).values
y = data['好瓜'].values
# 强制转换数据类型为数值类型
X = X.astype(np.float64)
# 用均值填充 NaN 值
col_mean = np.nanmean(X, axis=0)
inds = np.where(np.isnan(X))
X[inds] = np.take(col_mean, inds[1])
#数据标准化,按照列进行
X = (X - X.mean(axis=0)) / X.std(axis=0)
#添加偏置项
one_columns = np.ones((X.shape[0], 1))
X = np.hstack((one_columns, X))

#定义sigmod函数
def sigmoid(z):
    p = 1 / (1 + np.exp(-z))
    return p

#定义损失函数,theta是参数
#X是特征矩阵m X n ,y 是标签 m x 1
def compute_loss(X, y, theta):
    #计算样本个数
    m = len(y)
    #矩阵运算哈
    p = sigmoid(X @ theta)
    loss = -(y @ np.log(p) + (1 - y) @ np.log(1 - p)) / m
    return loss

# 梯度下降求解实现
# loss损失函数对theta求导之后得到梯度，这里计算有点复杂直接给结果 ：1/m x X的转置 x (h -y)
def compute_gradient(X, y, theta):
    m = len(y)
    h = sigmoid(X @ theta)
    gradient = (X.T @ (h - y)) / m
    return gradient

#Logistic_Regression
#步骤1.定义theta为全0向量 2.进行epochs 次下降 每次计算梯度并更新theta 3.返回优化的theta
def logistic_regression(X, y, lr=0.1, epochs=1000):
    m, n = X.shape
    theta = np.zeros(n)
    for _ in range(epochs):
        gradient = compute_gradient(X, y, theta)
        theta -= lr * gradient  # 梯度下降更新参数
    return theta

# 预测函数
def predict(X, theta):
    return (sigmoid(X @ theta) >= 0.5).astype(int)

#梯度下降优化参数
theta = logistic_regression(X, y, lr=0.01, epochs=100000)
y_pred = predict(X, theta)
print(y_pred)
#计算准确率
accuracy = np.mean(y_pred == y)
print(f"模型准确率:{accuracy:.2f}")

#万能解决图像不能显示中文的代码
plt.rcParams['font.sans-serif'] = ['SimHei']
plt.rcParams['axes.unicode_minus'] = False
# 计算混淆矩阵
conf_matrix = confusion_matrix(y, y_pred)
# 打印混淆矩阵
print("混淆矩阵：")
print(conf_matrix)
# 绘制混淆矩阵热力图
plt.figure(figsize=(8, 6))
sns.heatmap(conf_matrix, annot=True, fmt='d', cmap='Blues',
            xticklabels=['坏瓜', '好瓜'], yticklabels=['坏瓜', '好瓜'])
plt.xlabel('预测类别')
plt.ylabel('真实类别')
plt.title('混淆矩阵热力图')
plt.show()
# 生成分类报告
class_report = classification_report(y, y_pred, target_names=['坏瓜', '好瓜'])
print("分类报告：")
print(class_report)

#可视化分类结果，选取两个特征值
plt.figure(figsize=(8,6))
# 绘制分类结果
plt.scatter(X[:, 0], X[:, 1], c=y, cmap='bwr', edgecolors='k', label='样本分类')

# 计算决策边界
x_vals = np.linspace(X[:, 0].min(), X[:, 0].max(), 100)
y_vals = -(model.intercept_[0] + model.coef_[0][0] * x_vals) / model.coef_[0][1]
plt.plot(x_vals, y_vals, 'k--', label='决策边界')

plt.xlabel("特征1（标准化后）")
plt.ylabel("特征2（标准化后）")
plt.legend()
plt.title("逻辑回归分类结果")
plt.show()
