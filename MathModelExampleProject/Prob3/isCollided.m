% 这里写函数说明：检测碰撞的函数文件

function [isCollided] = isCollided(x, y, r, obs)
    % 初始化碰撞标志
    isCollided = 0;
    % 遍历所有障碍物
    for i = 1:size(obs, 1)
        % 计算障碍物中心到圆心的距离
        d = sqrt((obs(i, 1) - x)^2 + (obs(i, 2) - y)^2);
        % 如果距离小于半径和障碍物半径之和，则发生碰撞
        if d < r + obs(i, 3)
            isCollided = 1;
            break;
        end
    end
end