import axios from "axios";

// Set config defaults when creating the instance
const myAxios = axios.create({
    baseURL: 'http://localhost:8080/api',
});

myAxios.defaults.withCredentials = true;

// 添加请求拦截器
myAxios.interceptors.request.use(function (config) {
    console.log("手动响应,",config)
    return config;
}, function (error) {
    // 新增错误信息打印，方便调试
    console.error("响应错误:", error);

    // 对 401 等授权错误做统一处理（可选）
    if (error.response && error.response.status === 401) {
        console.log("未授权，可能需要重新登录");
        // 可以在这里跳转登录页：router.push('/login')
    }
    // 对请求错误做些什么
    return Promise.reject(error);
});

// 添加响应拦截器
myAxios.interceptors.response.use(
    function (response) {
        // 检查是否需要跳过拦截（通过请求配置中的自定义属性）
        const skipAuth = response.config.skipAuth;
        if (!skipAuth && response?.data?.code === 40100) {
            const redirectUrl = encodeURIComponent(window.location.href);
            window.location.href = `/user/login?redirect=${redirectUrl}`;
        }
        return response.data;
    },
    function (error) {
        // 错误响应也需要判断是否跳过拦截
        const skipAuth = error.config?.skipAuth;
        if (!skipAuth && error.response?.data?.code === 40100) {
            const redirectUrl = encodeURIComponent(window.location.href);
            window.location.href = `/user/login?redirect=${redirectUrl}`;
        }
        return Promise.reject(error);
    }
);

export default myAxios;