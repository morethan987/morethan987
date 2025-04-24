import pandas as pd 
import numpy as np
from sklearn import  linear_model
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import cross_val_score,LeaveOneOut,KFold
import matplotlib.pyplot as plt
data = pd.read_csv(r"Homework\ch3\3.4\iris\iris.data",delimiter=',',header=None)
X = data.iloc[:,:-1]
Y  = data.iloc[:,-1]
#花有三类
Y = Y.map({'Iris-setosa':0,'Iris-versicolor':1,'Iris-virginica':2})
#数据标准化
scale = StandardScaler()
X = scale.fit_transform(X)

model = linear_model.LogisticRegression(max_iter=1000)
predictions = model.fit(X,Y).predict(X)
print(predictions)
#10折交叉验证
kf = KFold(n_splits =10,shuffle = True,random_state=42)
kfold_scores = cross_val_score(model,X,Y,cv = kf,scoring='accuracy')
kfold_error = 1- np.mean(kfold_scores)

#留一法
loo_scores= cross_val_score(model,X,Y,cv =LeaveOneOut(),scoring='accuracy')
loo_error = 1- np.mean(loo_scores)

print(f"10折交叉验证法错误率:{kfold_error:.2f}")
print(f"留一法错误率:{loo_error:.2f}")
# 可视化预测结果
fig = plt.figure(figsize=(10, 8))
ax = fig.add_subplot(111, projection='3d')

# 绘制散点图
scatter = ax.scatter(X[:, 0], X[:, 1], X[:, 2], c=predictions, cmap='viridis')

# 添加颜色条
legend = fig.colorbar(scatter, ticks=[0, 1, 2])
legend.set_label('Predicted Class')

# 设置坐标轴标签
ax.set_xlabel('Sepal Length')
ax.set_ylabel('Sepal Width')
ax.set_zlabel('Petal Length')

# 设置标题
ax.set_title('Iris Flower Classification Predictions')

# 显示图形
plt.show()
    