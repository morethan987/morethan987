% 这里写函数说明：计算极坐标下点的坐标的函数

function val = getNextTheta(theta_prev, l)
    % 内部参数说明
    b = 0.55 / (2 * pi); % 螺旋线的系数
    r1 = b * theta_prev; % 当前的半径

    % 定义目标函数
    fun = @(theta_cur) sqrt(r1^2 + (b * theta_cur)^2 - 2 * r1 * (b * theta_cur) * cos(theta_cur - theta_prev)) - l;

    % 初始猜测值
    x0 = theta_prev;

    % 使用fsolve求解
    options = optimoptions('fsolve', 'Display', 'off'); % 禁止显示求解过程
    [val, ~] = fsolve(fun, x0, options);

end
