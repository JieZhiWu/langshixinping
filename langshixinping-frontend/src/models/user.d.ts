/**
 * 用户类别
 */
export type UserType = {
    id: number;
    username: string;
    userAccount: string;
    avatarUrl?: string;
    profile ?: string;
    gender: number;
    phone: string;
    email: string;
    userStatus: string;
    userRole: string;
    planetCode: string;
    tags: string[];
    createTime: Date;
};