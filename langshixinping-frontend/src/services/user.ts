import myAxios from "../plugins/myAxios.js";
import {getCurrentUserState, setCurrentUserState} from "../states/user";

/**
 * 获取用户信息
 * @returns {Promise<null|any>}
 */
/*
export const getCurrentUser = async () => {
    const user = getCurrentUserState();
    if (user) {
        return user;
    }
    //从远程处获取用户信息
    const res = await myAxios.get("/user/current", { skipAuth: true });
    if (res.code == 0 ) {
        setCurrentUserState(res.data);
        return res.data;
    }
    return null;
}
*/
export const getCurrentUser = async () => {
    try {
        // 带 skipAuth: true，避免拦截器跳转
        const res = await myAxios.get('/user/current', { skipAuth: true });
        if (res.code === 0) {
            return res.data; // 已登录，返回用户信息
        } else {
            return null; // 接口返回错误（未登录），返回 null
        }
    } catch (error) {
        return null; // 请求失败（如网络错误、未登录），返回 null
    }
};
