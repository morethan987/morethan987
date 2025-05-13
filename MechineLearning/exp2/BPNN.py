import sys
import numpy as np
from scipy.special import expit

class BPNN(object):
    def __init__(self,X,Y,q,l,epoch_times=1000,lr=0.001,visible=False):
        self.X=X # 训练样本->np.array
        self.Y=Y # 训练样本的标签->np.array
        self.n,self.d=self.X.shape # 样本形状
        self.q=q # 隐藏层神经元数量
        self.l=l # 输出层神经元数量，也就是分类数
        self.v,self.w=self.Init_wgt()
        self.gama,self.theta=self.Init_threshold() # 隐藏层和输出层神经元阈值
        self.epoch_times=epoch_times # 迭代次数
        self.lr=lr # 学习率
        self.visible=visible

    def Add_bias(self,X): #对X(1*d)添加偏置项->X_new(1*(d+1))
        X_new=np.ones((X.shape[0]+1))
        X_new[1:]=X
        return X_new
    def Init_wgt(self): #初始化v和w
        v=np.random.uniform(-1.0,1.0,size=(self.d+1)*self.q)
        v=v.reshape(self.d+1,self.q)
        w=np.random.uniform(-1.0,1.0,size=self.q*(self.l))
        w=w.reshape(self.q,self.l)
        return v,w
    def Init_threshold(self): #初始化gama和theta
        gama=np.random.uniform(-1.0,1.0,size=self.q)
        gama=gama.reshape(1,self.q)
        theta=np.random.uniform(-1.0,1.0,size=self.l)
        theta=theta.reshape(1,self.l)
        return gama,theta

    def Sigmoid(self,z):                #激活函数
        return expit(z)
    def Sigmoid_gradient(self,z):       #激活函数的梯度
        sg=self.Sigmoid(z)
        return sg*(1-sg)

    def FP(self,X):                     #将输入的训练/测试样本正向传播
        X_new=self.Add_bias(X)          #X_new:1*(d+1)
        alpha=np.matmul(X_new,self.v)   #alpha:1*q
        b=self.Sigmoid(alpha-self.gama) #b:1*q
        beita=np.matmul(b,self.w)       #beita:1*l
        y=self.Sigmoid(beita-self.theta)#y:1*l
        return alpha,b,beita,y

    def Get_cost(self,y,label):         #损失函数
        tmp=(y-label)*(y-label)/2
        cost=np.mean(tmp)
        return cost

    # 反向传播主函数
    def GD(self,X,b,y,label):
        g=y*(1-y)*(y-label) # 计算第输出层的误差
        tmp1=np.matmul(g,self.w.T)      #计算Σw[h][j]*g[j],tmp:1*q
        e=b*(1-b)*tmp1                  #b:1*q,e:1*q
        X_new=self.Add_bias(X)

        self.w-=self.lr*np.matmul(b.T,g)
        self.theta+=self.lr*g
        tmp2=X_new.T.reshape(-1,1)
        self.v-=self.lr*np.matmul(tmp2,e)
        self.gama+=self.lr*e
        return self

    def Pred(self,X): #对输入的`测试样本集`预测分类结果
        pred=[]
        for i in range(X.shape[0]):
            alpha,b,beita,y=self.FP(X[i])
            pred_tmp=np.argmax(y,axis=-1)   #预测值取最大的分类作为标签
            pred.append(pred_tmp[0])
        return pred

    def Train(self):
        costs=[] #存储每次训练后的损失函数
        for i in range(self.epoch_times):
            if self.visible:
                sys.stderr.write("\rEpoch times: %d/%d "%(i+1,self.epoch_times)+"▋"*(i//(self.epoch_times//20)))
                sys.stderr.flush()
            cost_tmp=[]
            for j in range(len(self.X)):#对每个样本进行训练
                alpha,b,beita,y=self.FP(self.X[j])
                cost=self.Get_cost(y,self.Y[j])
                cost_tmp.append(cost) # 记录损失
                self.GD(self.X[j],b,y,self.Y[j])
            costs.append(np.mean(np.array(cost_tmp)))
        return costs
