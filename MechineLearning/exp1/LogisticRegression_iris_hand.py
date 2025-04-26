import pandas as pd
import numpy as np
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import train_test_split
import itertools
from sklearn.metrics import accuracy_score, classification_report

# 读取数据
data = pd.read_csv(r"./data/iris.csv", delimiter=',', header=None)
X = data.iloc[1:, :-1]
Y = data.iloc[1:, -1]

# 花有三类
Y = Y.map({'setosa': 0, 'versicolor': 1, 'virginica': 2})

# 划分训练集和测试集
X_train, X_test, Y_train, Y_test = train_test_split(X, Y, test_size=0.2, random_state=42)

# 数据标准化
scale = StandardScaler()
X_train = scale.fit_transform(X_train)
X_test = scale.transform(X_test)


# 定义sigmoid函数
def sigmoid(z):
    return 1 / (1 + np.exp(-z))


# 自定义损失函数（交叉熵损失）
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
class_report = classification_report(Y_test, predictions_ovo, target_names=['setosa', 'versicolor', 'virginica'])
print("分类报告:")
print(class_report)
