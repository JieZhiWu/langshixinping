use langshi;

-- 插入普通用户1
INSERT INTO user (username, userAccount, avatarUrl, gender, userPassword, phone, email, userStatus, userRole, planetCode, tags)
VALUES ('张三', 'zhangsan123', 'https://picsum.photos/id/1/200', 1, '12345678', '13800138000', 'zhangsan@example.com', 0, 0, 'P10001', '["篮球","音乐","旅行"]');

-- 插入普通用户2
INSERT INTO user (username, userAccount, avatarUrl, gender, userPassword, phone, email, userStatus, userRole, planetCode, tags)
VALUES ('李四', 'lisi456', 'https://picsum.photos/id/2/200', 0, '87654321', '13900139000', 'lisi@example.com', 0, 0, 'P10002', '["编程","电影","美食"]');

-- 插入管理员用户
INSERT INTO user (username, userAccount, avatarUrl, gender, userPassword, phone, email, userStatus, userRole, planetCode, tags)
VALUES ('管理员', 'admin', 'https://picsum.photos/id/3/200', 1, 'admin123', '13600136000', 'admin@example.com', 0, 1, 'P00001', '["系统管理","权限分配"]');

-- 插入禁用状态用户
INSERT INTO user (username, userAccount, avatarUrl, gender, userPassword, phone, email, userStatus, userRole, planetCode, tags)
VALUES ('王五', 'wangwu789', 'https://picsum.photos/id/4/200', 1, 'wuwang5678', '13700137000', 'wangwu@example.com', 1, 0, 'P10003', '["游戏","摄影"]');

-- 插入女性用户
INSERT INTO user (username, userAccount, avatarUrl, gender, userPassword, phone, email, userStatus, userRole, planetCode, tags)
VALUES ('赵六', 'zhaoliu666', 'https://picsum.photos/id/5/200', 2, 'liuzhao8765', '13500135000', 'zhaoliu@example.com', 0, 0, 'P10004', '["瑜伽","阅读","绘画"]');
