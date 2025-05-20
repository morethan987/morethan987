from DecisionTree import DT_clf
import pandas as pd
from sklearn.model_selection import train_test_split#随机划分样本集

#读取红酒品质数据集
f=pd.read_excel(r'exp3/data/winequality_data.xlsx')
X=f.iloc[:,:11].values              #属性值,numpy数组
y=f.iloc[:,11].values               #标签值,numpy数组
feature_name=f.columns.tolist()[:11]#特征的列表
#按7:3划分训练集和测试集(numpy数组)
X_train,X_test,y_train,y_test=train_test_split(X,y,test_size=0.3)

#训练模型并计算准确率
tree=DT_clf(list(X_train),list(y_train),feature_name,7)
tree.Train()
acc,y_pred=tree.Test(list(X_test),list(y_test))
print('Accuracy of classification: %.2f%%'%(acc*100))
print('Visualization of Decision Tree:')
print(tree.LevelOrder(tree.root))

tree.plot_tree()