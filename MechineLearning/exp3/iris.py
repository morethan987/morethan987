'''鸢尾花数据集
    150个样本,按7:3划分为训练集和测试集
    3个类别('Iris-setosa', 'Iris-versicolor', 'Iris-virginica'),
    4个属性('sepal length','sepal width','petal length','petal width'),
    构建决策树分类模型并测试准确率
    呈现训练过程,绘制分类结果,评估模型准确性
'''
import numpy as np
from DecisionTree import CART_clf
import pandas as pd
from sklearn.model_selection import train_test_split#随机划分样本集

#读取鸢尾花数据集
f=pd.read_excel(r'iris_data.xlsx')
X=f.iloc[:,:4].values                               #属性值,numpy数组
y=f.iloc[:,4].values                                #标签值,numpy数组
feature_name=f.columns.tolist()[:4]                 #特征的列表
#按7:3划分训练集和测试集(numpy数组)
X_train,X_test,y_train,y_test=train_test_split(X,y,test_size=0.3)

#训练模型并计算准确率
cart=CART_clf(list(X_train),list(y_train),feature_name,4)
cart.Train()
acc,y_pred=cart.Test(list(X_test),list(y_test))
print('Accuracy of classification: %.2f%%'%(acc*100))
print('Visualization of Decision Tree:')
print(cart.LevelOrder(cart.root))

#将数据合并成[属性,标签]形式
data_real=[]
data_pred=[]
for i in range(len(y_test)):
    tmp1=list(X_test[i]).copy()
    tmp1.append(y_test[i])
    data_real.append(tmp1)
    tmp2=list(X_test[i]).copy()
    tmp2.append(y_pred[i])
    data_pred.append(tmp2)
tmp3=feature_name.copy()
tmp3.append('Real_label')
tmp4=feature_name.copy()
tmp4.append('Pred_label')
data_real=pd.DataFrame(data_real,columns=tmp3)
data_pred=pd.DataFrame(data_pred,columns=tmp4)

#绘制多维数据的平行坐标
cart.ParallelCoordinates(data_real,data_pred)

#绘制RadViz雷达图
cart.Radviz(data_real,data_pred)

#绘制相关系数热力图
cart.Heatmap(data_real,data_pred)

#绘制所谓数据的矩阵图,表示不同特征之间的关系
cart.MatrixPlot(data_real,data_pred)