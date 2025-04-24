import numpy as np
import pandas as pd
from matplotlib import pyplot as plt
from sklearn.linear_model import LogisticRegression
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import accuracy_score,confusion_matrix,classification_report
import seaborn as sns
#导入数据，处理数据
data = pd.read_csv('./Homework/ch3/3_3/watermelon3_0a.csv',encoding = 'utf-8')
#数据有非数值属性，我们需要对其进行编码,这里使用one_hot 编码，以避免奇怪的数值关系
categorical_columns = ['色泽','根蒂','敲声','纹理','脐部','触感']
data = pd.get_dummies(data,columns =categorical_columns)
#目标变量编码
data['好瓜'] = data['好瓜'].replace({'是':1,'否':0})
#选择特征和目标变量
X = data.drop(columns=['好瓜','编号']).values
y = data['好瓜'].values

scaler = StandardScaler()
X = scaler.fit_transform(X)

model =  LogisticRegression(max_iter=100000)
model.fit(X,y)
y_pred = model.predict(X)
accuracy = accuracy_score(y,y_pred)
print(y_pred)
print(f"模型准确率：{accuracy:.2f}")
#万能解决图像不能显示中文的代码
plt.rcParams['font.sans-serif']=['SimHei']
plt.rcParams['axes.unicode_minus']=False
'''
#万能解决图像不能显示中文的代码
plt.rcParams['font.sans-serif']=['SimHei']
plt.rcParams['axes.unicode_minus']=False
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
'''
#可视化分类结果，选取两个特征值
plt.figure(figsize=(8,6))
# 绘制分类结果
plt.scatter(X[:, 0], X[:, 1], c=y, cmap='bwr', edgecolors='k', label='样本分类')

# 计算决策边界
x_vals = np.linspace(X[:, 0].min(), X[:, 0].max(), 100)
y_vals = -(model.intercept_[0] + model.coef_[0][0] * x_vals) / model.coef_[0][1]
#plt.plot(x_vals, y_vals, 'k--', label='决策边界')

plt.xlabel("特征1（标准化后）")
plt.ylabel("特征2（标准化后）")
plt.legend()
plt.title("逻辑回归分类结果")
plt.show()
