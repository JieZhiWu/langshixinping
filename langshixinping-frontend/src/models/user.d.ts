/**
 * 用户类别
 */
export type UserType = {
    id: number;
    username: string;
    userAccount: string;
    avatarUrl: string;
    gender: number; // 0-未填写, 1-男, 2-女
    userPassword?: string; // 通常不返回，可以设置为可选
    phone?: string;
    email?: string;
    userStatus: number; // 0-正常
    createTime: string; // ISO 时间字符串
    updateTime: string;
    isDelete: number; // 逻辑删除
    userRole: number; // 0-普通用户, 1-管理员
    planetCode?: string;
    tags?: string; // 存 JSON 字符串
    profile?: string; // 个人简介
};