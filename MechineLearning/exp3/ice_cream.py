import pandas as pd
from DecisionTree import DT_reg
from sklearn.model_selection import train_test_split    #随机划分样本集

#读取冰淇淋数据集
f=pd.read_excel(r'exp3/data/icecream_data.xlsx')
X=f.iloc[:,:1].values               #属性值,numpy数组
y=f.iloc[:,1].values                #标签值,numpy数组
feature_name=f.columns.tolist()[:1] #特征的列表
#按7:3划分训练集和测试集(numpy数组)
X_train,X_test,y_train,y_test=train_test_split(X,y,test_size=0.3)

#训练模型并计算R^2
tree=DT_reg(list(X_train),list(y_train),feature_name,8)
tree.Train()
R_square,y_pred=tree.Test(list(X_test),list(y_test))
print('R_square of regression: %.2f'%R_square)
print('Visualization of Decision Tree:')
print(tree.LevelOrder(tree.root))

tree.plot_tree()