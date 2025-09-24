import {UserType} from "./user";

/**
 * 队伍类别
 */
export type TeamType = {
    id: number;
    name: string;
    description?: string;
    maxNum: number;
    expireTime?: string; // yyyy-MM-dd HH:mm:ss
    userId: number; // 队长 ID
    status: number; // 0 - 公开，1 - 私有，2 - 加密
    password?: string;
    createTime: string;
    updateTime: string;
    isDelete: number;
    createUser?: UserType;
    hasJoin?: boolean;
    hasJoinNum?: number;
};
