import pandas as pd
import numpy as np
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import train_test_split, cross_val_score, LeaveOneOut, KFold
import itertools
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
from sklearn.metrics import accuracy_score, confusion_matrix, classification_report
import seaborn as sns

# 读取数据
data = pd.read_csv(r"Homework\ch3\3.4\iris\iris.data", delimiter=',', header=None)
X = data.iloc[:, :-1]
Y = data.iloc[:, -1]

# 花有三类
Y = Y.map({'Iris-setosa': 0, 'Iris-versicolor': 1, 'Iris-virginica': 2})

# 划分训练集和测试集
X_train, X_test, Y_train, Y_test = train_test_split(X, Y, test_size=0.2, random_state=42)

# 数据标准化
scale = StandardScaler()
X_train = scale.fit_transform(X_train)
X_test = scale.transform(X_test)


# 定义sigmoid函数
def sigmoid(z):
    return 1 / (1 + np.exp(-z))


# 自定义损失函数（对数损失）
def log_loss(y_true, y_pred):
    epsilon = 1e-15
    y_pred = np.clip(y_pred, epsilon, 1 - epsilon)
    return -np.mean(y_true * np.log(y_pred) + (1 - y_true) * np.log(1 - y_pred))


# 梯度下降求解
def gradient_descent(X, y, learning_rate, num_iterations):
    num_samples, num_features = X.shape
    theta = np.zeros(num_features)
    for _ in range(num_iterations):
        z = np.dot(X, theta)
        y_pred = sigmoid(z)
        gradient = np.dot(X.T, (y_pred - y)) / num_samples
        theta = theta - learning_rate * gradient
    return theta


# 手动实现OvO策略
# 生成所有类别对
class_pairs = list(itertools.combinations(np.unique(Y_train), 2))
num_classifiers = len(class_pairs)
classifiers = []

# 训练每个二分类器
for pair in class_pairs:
    # 选择当前类别对的数据
    pair_indices = np.logical_or(Y_train == pair[0], Y_train == pair[1])
    X_pair = X_train[pair_indices]
    Y_pair = Y_train[pair_indices]
    # 将类别标签转换为0和1
    Y_pair_binary = (Y_pair == pair[1]).astype(int)

    # 添加偏置项
    X_pair = np.c_[np.ones((X_pair.shape[0], 1)), X_pair]

    # 使用梯度下降求解参数
    theta = gradient_descent(X_pair, Y_pair_binary, learning_rate=0.1, num_iterations=500)
    classifiers.append(theta)


# 预测函数
def predict_ovo(X, classifiers, class_pairs):
    num_samples = X.shape[0]
    votes = np.zeros((num_samples, len(np.unique(Y))))
    for i, theta in enumerate(classifiers):
        pair = class_pairs[i]
        X_with_bias = np.c_[np.ones((num_samples, 1)), X]
        z = np.dot(X_with_bias, theta)
        y_pred = sigmoid(z)
        predictions = (y_pred >= 0.5).astype(int)
        for j in range(num_samples):
            if predictions[j] == 0:
                votes[j, pair[0]] += 1
            else:
                votes[j, pair[1]] += 1
    final_predictions = np.argmax(votes, axis=1)
    return final_predictions


# 在测试集上进行预测
predictions_ovo = predict_ovo(X_test, classifiers, class_pairs)
print(predictions_ovo)
print(f"模型在测试集上的准确率: {accuracy_score(Y_test, predictions_ovo):.2f}")
# 生成分类报告
class_report = classification_report(Y_test, predictions_ovo, target_names=['Iris-setosa', 'Iris-versicolor', 'Iris-virginica'])
print("分类报告:")
print(class_report)
'''
# 绘制混淆矩阵
conf_matrix = confusion_matrix(Y_test, predictions_ovo)
plt.figure(figsize=(8, 6))
sns.heatmap(conf_matrix, annot=True, fmt='d', cmap='Blues', 
            xticklabels=['Iris-setosa', 'Iris-versicolor', 'Iris-virginica'], 
            yticklabels=['Iris-setosa', 'Iris-versicolor', 'Iris-virginica'])
plt.xlabel('predicted class')
plt.ylabel('true class')
plt.title('Confusion Matrix')
plt.show()

# 分类结果可视化
fig = plt.figure(figsize=(10, 8))
ax = fig.add_subplot(111, projection='3d')

# 绘制散点图
scatter = ax.scatter(X_test[:, 0], X_test[:, 1], X_test[:, 2], c=predictions_ovo, cmap='viridis')

# 添加颜色条
legend = fig.colorbar(scatter, ticks=[0, 1, 2])
legend.set_label('Predicted Class')

# 设置坐标轴标签
ax.set_xlabel('Sepal Length')
ax.set_ylabel('Sepal Width')
ax.set_zlabel('Petal Length')

# 设置标题
ax.set_title('Iris Flower Classification Predictions (OvO)')

# 显示图形
plt.show()

learning_rates = [0.001, 0.01, 0.1]
num_iterations_list = [500, 1000, 2000]

best_accuracy = 0
best_params = {}

for learning_rate in learning_rates:
    for num_iterations in num_iterations_list:
        classifiers = []
        for pair in class_pairs:
            pair_indices = np.logical_or(Y_train == pair[0], Y_train == pair[1])
            X_pair = X_train[pair_indices]
            Y_pair = Y_train[pair_indices]
            Y_pair_binary = (Y_pair == pair[1]).astype(int)
            X_pair = np.c_[np.ones((X_pair.shape[0], 1)), X_pair]
            theta = gradient_descent(X_pair, Y_pair_binary, learning_rate=learning_rate, num_iterations=num_iterations)
            classifiers.append(theta)
        
        predictions_ovo = predict_ovo(X_test, classifiers, class_pairs)
        accuracy = accuracy_score(Y_test, predictions_ovo)
        
        if accuracy > best_accuracy:
            best_accuracy = accuracy
            best_params = {'learning_rate': learning_rate, 'num_iterations': num_iterations}

print("最佳参数:", best_params)
print("最佳准确率:", best_accuracy)
'''