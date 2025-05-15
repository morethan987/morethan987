import numpy as np
import queue
import matplotlib.pyplot as plt


class Node:
    '''节点类
        由于属性都是连续值,连续属性离散化采用的是二分思想,因此决策树是二叉树,节点只有左右子树
        为了存储当前节点的最优划分属性及其对应的最优划分点,Node类加入了feature和value属性
        当Node为中间节点时,feature和value存储最优划分属性和值,left和right分别存储feature取值小于等于value和大于value的样本构成的Node
        当Node为叶节点时,feature存储输出值(分类标签或回归值),value无意义
    '''
    def __init__(self,feature,value,level=0,left=None,right=None):
        '''args:
            feature:最优划分属性或叶节点的输出值
            value:连续属性的最优划分点(float)
            level:节点的层级(int)
            left:左子树(Node)
            right:右子树(Node)
        '''
        self.feature=feature
        self.value=value
        self.level=level
        self.left=left
        self.right=right
        
    def Infm(self):
        '''决策树遍历树时打印节点信息'''
        infm='Level '+str(self.level)+'  feature:'+str(self.feature)+',value: '+str(self.value)
        return infm


class BaseCART:
    '''CART决策树基类
        包含分类树和回归树的共同属性和方法
        决策树类在初始化时只需要提供样本的基本数据,即属性值、属性名称、输出值
        为了方便建树时节点分割,样本索引列表中均只存储各结点在整个类的训练集X中的索引
        为了防止过拟合,可以指定树的最大深度
        为了便于对测试集进行预测,建立属性值->索引的映射
    '''
    def __init__(self,X,y,feature_name,MaxLevel=10):
        '''args:
            X:训练样本的属性(list)
            y:训练样本的输出值(分类标签或回归值)(list)
            feature_name:属性名称(list)
            MaxLevel:最大深度(int)
            feature_index:属性对应的索引
            root:决策树根节点
        '''
        self.X=X
        self.y=y
        self.feature_name=feature_name
        self.MaxLevel=MaxLevel
        self.feature_index=self.getFeatureIndex()
        self.root=None
        
    def getFeatureIndex(self):
        '''建立属性值->索引的映射,返回字典'''
        feature_dict={}
        for i in range(len(self.feature_name)):
            feature_dict[self.feature_name[i]]=i
        return feature_dict

    def Continuity2Discrete(self,Da):
        '''连续属性离散化:排序->计算相邻点中点
            args:
                Da:当前数据集在属性a上的[索引,属性值]列表(list)
            return:
                Da_sort:对属性值进行排序的[索引,属性值]列表(list)
                T:候选划分点(list)
        '''
        Da_sort=sorted(Da,key=lambda x:x[1])        #对第1列(属性值)进行排序
        index=[x[0] for x in Da_sort]               #按属性值升序排列的数据在self.X中索引值
        A=[x[1] for x in Da_sort]                   #升序排列的属性值
        T=[(A[i]+A[i+1])/2 for i in range(len(A)-1)]#候选划分点
        return Da_sort,T
        
    def Train(self):
        '''构建决策树,将根节点赋给self.root'''
        print('Start building CART.')
        D=[i for i in range(len(self.y))]
        self.root=self.BuildTree(D,0)
        print('Finish building CART.')
        return

    def preOrder(self,root):
        '''前序遍历决策树'''
        if root==None:
            return
        print(root.Infm())
        self.preOrder(root.left)
        self.preOrder(root.right)
        return

    def LevelOrder(self,root):
        '''层序遍历决策树'''
        q=queue.Queue()
        q.put(root)
        while(q.qsize()>0):
            node=q.get()
            print(node.Infm())
            if node.left is not None:
                q.put(node.left)
                q.put(node.right)
        return

    def Pred(self,x):
        '''对单一测试样本进行预测
            args:
                x:一个测试样本的属性(list)
            return:
                输出值:分类标签或回归值
        '''
        ptr=self.root
        while ptr is not None:
            if ptr.left is None and ptr.right is None:  #ptr为叶节点
                return ptr.feature
            fi=self.feature_index[ptr.feature]
            if x[fi]<=ptr.value:
                ptr=ptr.left
            else:
                ptr=ptr.right

    def getBestSplit(self,D):
        '''寻找最优划分(属性及其对应二分边界)的抽象方法
           需要在子类中实现具体的划分准则
        '''
        raise NotImplementedError

    def BuildTree(self,D,level):
        '''构建决策树的抽象方法
           需要在子类中实现具体的建树规则
        '''
        raise NotImplementedError

    def Test(self,X_test,y_test):
        '''测试模型的抽象方法
           需要在子类中实现具体的评估指标
        '''
        raise NotImplementedError

    def plot_tree(self, figsize=(12, 8)):
        '''绘制决策树的可视化图
        args:
            figsize: 图像大小元组 (width, height)
        '''
        if self.root is None:
            print("决策树还未训练，请先调用Train()方法")
            return
        
        plt.figure(figsize=figsize)
        
        def get_tree_depth(node):
            if node is None:
                return 0
            return max(get_tree_depth(node.left), get_tree_depth(node.right)) + 1
        
        def get_tree_width(node, level, width_dict):
            if node is None:
                return
            width_dict[level] = width_dict.get(level, 0) + 1
            get_tree_width(node.left, level + 1, width_dict)
            get_tree_width(node.right, level + 1, width_dict)
        
        total_depth = get_tree_depth(self.root)
        width_dict = {}
        get_tree_width(self.root, 0, width_dict)
        
        def plot_node(node, x, y, dx, dy, level):
            if node is None:
                return
                
            # 绘制节点
            if node.left is None and node.right is None:  # 叶节点
                color = 'lightgreen'
                text = f'{node.feature}'
            else:  # 决策节点
                color = 'lightblue'
                text = f'{node.feature}\n≤ {node.value:.2f}'
                
            plt.plot(x, y, 'o', markersize=30, color=color)
            plt.text(x, y, text, ha='center', va='center', fontsize=8)
            
            # 绘制到子节点的连接线
            if node.left:
                plt.plot([x, x-dx], [y, y-dy], '-', color='gray')
                plot_node(node.left, x-dx, y-dy, dx/2, dy, level+1)
            if node.right:
                plt.plot([x, x+dx], [y, y-dy], '-', color='gray')
                plot_node(node.right, x+dx, y-dy, dx/2, dy, level+1)
        
        # 计算初始间距
        dx = 1
        dy = 1 / (total_depth)
        
        # 绘制树
        plot_node(self.root, 0.5, 1-dy/2, dx/2, dy, 0)
        
        plt.axis('off')
        plt.title('Decision Tree Visualization')
        plt.show()


class CART_clf(BaseCART):
    '''CART分类树
       采用基尼指数作为分裂准则
       叶节点的输出为该节点中样本最多的类别
    '''
    def Gini(self,Dv):
        '''Gini(Dv)=1-Σpk^2,k遍历不同的分类属性
            计算样本集Dv的纯度,即随机抽取的两个样本不属于一类的概率
            args:
                Dv:当前选定数据集的索引列表(list)
            return:
                Gini(Dv):基尼值(float)
        '''
        #根据Dv中索引号获取对应label
        label=np.array([self.y[i] for i in Dv])     #当前选定数据集Dv的标签
        #统计各feature在当前样本中占比
        if len(label)==0:
            return 1
        p=np.array([label[label==k].size/len(label) for k in set(label)])
        return 1-np.sum(p*p)

    def Gini_index(self,D,a,t):
        '''Gini_index(D,a)=ΣGini(Dv)*|Dv|/|D|,k遍历不同的属性取值
            由于是采用二分思想的连续属性离散化,因此属性取值只有两项
            D1={x in D | x.a<=t};D2={x in D | x.a>t}
            args:
                D:当前数据集D的索引列表(list)
                a:属性a在feature_name中的索引(int)
                t:属性a的二分边界值(float)
            return:
                Gini_index(D,a):基尼指数(float)
        '''
        D1=[x for x in D if self.X[x][a]<=t]
        D2=[x for x in D if self.X[x][a]>t]
        return D1,D2,(self.Gini(D1)*len(D1)+self.Gini(D2)*len(D2))/len(D)

    def getBestSplit(self,D):
        '''寻找当前D中最优划分(属性及其对应二分边界)
            args:
                D:当前数据集D的索引列表(list)
            return:
                D1_final:左子树节点的索引列表(list)
                D2_final:右子树节点的索引列表(list)
                gini_index_final:最小基尼指数(float)
                a_final:最优划分属性的索引(int)
                t_final:最优划分属性的最优划分值(float)
        '''
        D1_final=None
        D2_final=None
        gini_index_final=float('inf')
        a_final=-1
        t_final=0
        for a in range(len(self.feature_name)):     #遍历属性
            Da=[[i,self.X[i][a]] for i in D]        #D中样本的属性a上取值的列表
            Da_sort,T=self.Continuity2Discrete(Da)  #返回对属性值进行排序的[索引,属性值]列表和候选划分点
            for t in T:
                D1,D2,gini_index=self.Gini_index(D,a,t)
                if gini_index<gini_index_final:
                    gini_index_final=gini_index
                    D1_final=D1
                    D2_final=D2
                    a_final=a
                    t_final=t
        return D1_final,D2_final,gini_index_final,a_final,t_final

    def BuildTree(self,D,level):
        '''递归建树
            args:
                D:当前数据集D的索引列表(list)
                level:当前深度(int)
            return:
                root:建树的根节点(Node)
        '''
        if len(D)==0:
            return None
        label=[self.y[x] for x in D]              #当前数据集D的样本分类标签
        label_final=max(set(label),key=label.count)
        if level>=self.MaxLevel:                  #超过最大深度,直接停止,分类值为出现最多的label
            return Node(label_final,None,level)
        elif len(set(label))==1:
            return Node(self.y[D[0]],None,level)  #所有节点属于一个类,该节点为叶结点
        else:
            lc,rc,gini,a,t=self.getBestSplit(D)
            root=Node(self.feature_name[a],t,level)
            root.left=self.BuildTree(lc,level+1)
            root.right=self.BuildTree(rc,level+1)
            return root

    def Test(self,X_test,y_test):
        '''对测试集进行分类,统计准确率
            args:
                X_test:测试样本集的属性(list)
                y_test:测试样本集的分类标签
            return:
                acc:准确率
                y_pred:预测分类
        '''
        y_pred=[]                                   #记录预测分类
        cnt=0                                       #记录分类正确的样本数
        for i in range(len(X_test)):
            x=X_test[i]
            pred=self.Pred(x)
            y_pred.append(pred)
            if pred==y_test[i]:
                cnt+=1
        acc=cnt/len(y_test)
        return acc,y_pred


class CART_reg(BaseCART):
    '''CART回归树
       采用RSS(残差平方和)作为分裂准则
       叶节点的输出为该节点中样本的均值
    '''
    def getRSS(self,R,j,s):
        '''RSS=Σ(R1)(yi-yRj)^2Σ(R2)(yi-yRj)^2
            R1={x in R | x.j<=s};R2={x in R | x.j>s}
            args:
                R:当前数据集R的索引列表(list)
                j:属性j在feature_name中的索引(int)
                s:属性j的二分边界值(float)
            return:
                RSS(R,j,s):RSS(float)
        '''
        R1=[x for x in R if self.X[x][j]<=s]
        R2=[x for x in R if self.X[x][j]>s]
        YR1=np.mean(np.array([self.y[i] for i in R1]))
        YR2=np.mean(np.array([self.y[i] for i in R2]))
        Y1=[self.y[i] for i in R1]
        Y2=[self.y[i] for i in R2]
        RSS=np.sum(np.square(Y1-YR1))+np.sum(np.square(Y2-YR2))
        return R1,R2,RSS

    def getBestSplit(self,R):
        '''寻找当前R中最优划分(属性及其对应二分边界)
            args:
                R:当前数据集R的索引列表(list)
            return:
                R1_final:左子树节点的索引列表(list)
                R2_final:右子树节点的索引列表(list)
                RSS_final:最小RSS值(float)
                j_final:最优划分属性的索引(int)
                s_final:最优划分属性的最优划分值(float)
        '''
        R1_final=None
        R2_final=None
        RSS_final=float('inf')
        j_final=-1
        s_final=0
        for j in range(len(self.feature_name)):     #遍历属性
            Rj=[[i,self.X[i][j]] for i in R]        #R中样本的属性j上取值的列表
            Rj_sort,S=self.Continuity2Discrete(Rj)  #返回对属性值进行排序的[索引,属性值]列表和候选划分点
            for s in S:
                R1,R2,RSS=self.getRSS(R,j,s)
                if RSS<RSS_final:
                    RSS_final=RSS
                    R1_final=R1
                    R2_final=R2
                    j_final=j
                    s_final=s
        return R1_final,R2_final,RSS_final,j_final,s_final

    def BuildTree(self,R,level):
        '''递归建树
            args:
                R:当前数据集R的索引列表(list)
                level:当前深度(int)
            return:
                root:建树的根节点(Node)
        '''
        if len(R)==0:
            return None
        reg=[self.y[x] for x in R]                #当前数据集R的样本回归值
        reg_final=np.mean(np.array(reg))
        if level>=self.MaxLevel:                  #超过最大深度,直接停止,回归值为reg的均值
            return Node(reg_final,None,level)
        elif len(R)==1:
            return Node(self.y[R[0]],None,level)  #只有一个样本,该节点为叶结点
        else:
            lc,rc,RSS,j,s=self.getBestSplit(R)
            root=Node(self.feature_name[j],s,level)
            root.left=self.BuildTree(lc,level+1)
            root.right=self.BuildTree(rc,level+1)
            return root

    def Test(self,X_test,y_test):
        '''对测试集进行预测,计算R方值
            args:
                X_test:测试样本集的属性(list)
                y_test:测试样本集的真实回归值
            return:
                R_square:R^2值
                y_pred:预测结果
        '''
        y_pred=[]                                   #记录预测结果
        top=0                                       #分子
        bottom=0                                    #分母
        yi=np.mean(np.array(y_test))               #真实回归值的均值
        for i in range(len(X_test)):
            pred=self.Pred(X_test[i])
            y_pred.append(pred)
            top+=(y_test[i]-pred)*(y_test[i]-pred)
            bottom+=(y_test[i]-yi)*(y_test[i]-yi)
        return 1-top/bottom,y_pred
