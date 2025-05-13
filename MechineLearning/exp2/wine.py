from xlrd import open_workbook
from BPNN import BPNN
import numpy as np
import matplotlib.pyplot as plt

#读取数据并将其按7:3划分为训练集和测试集
path=r"exp2/data/winequality_data.xlsx" # kaggle红酒品质数据集
book=open_workbook(path)
sheet=book.sheets()[0]          #打开sheet0
nrow=sheet.nrows                #行数
ncol=sheet.ncols                #列数
label_dic={'Bad':0,'Normal':1,'Good':2}     #标签序列化
data_train=[]                   #训练集属性(n*d)
data_test=[]                    #训练集标签(n*l)
label_train=[]                  #测试集属性
label_test=[]                   #测试集标签(one-hot)
labeln_test=[]                  #测试集标签(分类值)
for i in range(1,nrow):
    row=sheet.row_values(i)
    one_hot=[0]*len(label_dic)  #样本分类标签的one-hot向量
    label=label_dic[row[11]]
    one_hot[label]=1
    row.pop(11)                 #保留样本属性,删除标签
    if(i%10<7):
        data_train.append(row)
        label_train.append(one_hot)
    else:
        data_test.append(row)
        label_test.append(one_hot)
        labeln_test.append(label)
data_train=np.array(data_train)
data_test=np.array(data_test)
label_train=np.array(label_train)
label_test=np.array(label_test)
labeln_test=np.array(labeln_test)

#实例化BPNN对象并进行训练
d=len(data_train[0])            #属性数
l=len(label_dic)                #分类数
n=nrow                          #样本数
ep=10000                      #训练次数
q=30                            #隐藏层神经元数
bpnn=BPNN(data_train,label_train,q,l,ep,0.001,True)
cst=bpnn.Train()                #训练过程的损失函数
pred=bpnn.Pred(data_test)       #训练后的分类结果

#计算准确率
acc=0
for i in range(len(pred)):
    if(pred[i]==labeln_test[i]):
        acc+=1
acc/=len(pred)
print('\nAccuracy of classification: %.2f%%'%(acc*100))

#绘制预测值/标签值-编号的图像
plt.plot(pred,marker='+')
plt.plot(labeln_test,marker='*')
plt.ylabel('Prediction & Label')
plt.xlabel('number of sample')
plt.legend(['Pred_label','Real_Label'])
plt.show()

#绘制损失函数
plt.plot(range(len(cst)),cst)
plt.ylabel('Cost')
plt.xlabel('Epoch_times')
plt.show()