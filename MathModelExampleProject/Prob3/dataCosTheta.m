% 这里写函数说明：数据清洗文件，生成dataCosTheta.mat数据文件

clc, clear;
run("config.m");

load("r.mat")
load("theta.mat")

%初始化xy轴坐标
x = zeros(224, 301);
y = zeros(224, 301);

%计算xy轴坐标
for i = 1:224

    for j = 1:301
        x_cur = r(i, j) * cos(theta(i, j));
        y_cur = r(i, j) * sin(theta(i, j));
        x(i, j) = x_cur;
        y(i, j) = y_cur;
    end

end

save("x", "x");
save("y", "y");
