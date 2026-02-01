import axios from 'axios';
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus';

/** 这两个封装方法适合有返回值的请求 **/

// get请求
export function get(url, config) {

  // 请求超过30秒则判定为超时
  const instance = axios.create({
    baseURL: '/api',
    timeout: 30000,
    withCredentials: true,
  });

  // axios拦截器
  // 请求拦截
  instance.interceptors.request.use(
    (config) => {
      // 自动添加 token 到请求头
      const token = localStorage.getItem('teri_token');
      if (token) {
        config.headers.Authorization = 'Bearer ' + token;
      }
      return config;
    },
    (err) => {
      console.log(err);
    },
  );

  // 响应拦截
  instance.interceptors.response.use(
    (config) => {
      const code = config.data.code;
      if (code && code !== 200)
        ElMessage.error(config.data.message || '未知错误, 请打开控制台查看');
      return config;
    },
    (err) => {
      console.log(err);
      const userStore = useUserStore();
      
      // Handle 401 Unauthorized
      if (err.response && err.response.status === 401) {
        // 只有在有 token 的情况下才清除登录状态
        const hasToken = localStorage.getItem("teri_token");
        if (hasToken) {
          // 修改当前的登录状态
          userStore.initData();
          // 关闭websocket
          if (userStore.ws) {
            userStore.ws.close();
            userStore.ws = null;
          }
          // 清除本地token缓存
          localStorage.removeItem("teri_token");
          ElMessage.error("登录已过期，请重新登录");
        }
        userStore.isLoading = false;
        return Promise.reject(err);
      }
      
      if (err.response && err.response.headers && err.response.headers.message === 'not login') {
        // 只有在有 token 的情况下才清除登录状态
        const hasToken = localStorage.getItem("teri_token");
        if (hasToken) {
          // 修改当前的登录状态
          userStore.initData();
          // 关闭websocket
          if (userStore.ws) {
            userStore.ws.close();
            userStore.ws = null;
          }
          // 清除本地token缓存
          localStorage.removeItem("teri_token");
          ElMessage.error("登录已过期，请重新登录");
        }
        userStore.isLoading = false;
      } else {
        // Only show error for non-404 errors
        if (!err.response || err.response.status !== 404) {
          ElMessage.error("特丽丽被玩坏了(¯﹃¯)");
        }
        userStore.isLoading = false;
      }
      return Promise.reject(err);
    },
  );

  instance.defaults.withCredentials = true;

  // axios.get("http://xxx/xxx",{
  //   //参数列表
  //   params:{ id: id},
  //   //请求头配置  
  //   headers:{ token: token }
  // })

  if (config) {
    if (config.params) {
      if (config.headers) {
        return instance.get(url, {params: config.params, headers: config.headers}); // 有参数和请求头
      }
      return instance.get(url, {params: config.params}); // 有参数没请求头
    }
    if (config.headers) {
      return instance.get(url, {headers: config.headers}); // 没参数有请求头
    }
  } else {
    return instance.get(url); // 没参数和请求头
  }

}

// post请求
export function post(url, data, headers) {

  // 请求超过30秒则判定为超时
  const instance = axios.create({
    baseURL: '/api',
    timeout: 30000,
    withCredentials: true,
  });

  // axios拦截器
  // 请求拦截
  instance.interceptors.request.use(
    (config) => {
      // 自动添加 token 到请求头
      const token = localStorage.getItem('teri_token');
      if (token) {
        config.headers.Authorization = 'Bearer ' + token;
      }
      return config;
    },
    (err) => {
      console.log(err);
    },
  );

  // 响应拦截
  instance.interceptors.response.use(
    (config) => {
      const code = config.data.code;
      if (code && code !== 200)
        ElMessage.error(config.data.message || '未知错误, 请打开控制台查看');
      return config;
    },
    (err) => {
      console.log(err);
      const userStore = useUserStore();
      
      // Handle 401 Unauthorized
      if (err.response && err.response.status === 401) {
        // 只有在有 token 的情况下才清除登录状态
        const hasToken = localStorage.getItem("teri_token");
        if (hasToken) {
          // 修改当前的登录状态
          userStore.initData();
          // 关闭websocket
          if (userStore.ws) {
            userStore.ws.close();
            userStore.ws = null;
          }
          // 清除本地token缓存
          localStorage.removeItem("teri_token");
          ElMessage.error("登录已过期，请重新登录");
        }
        userStore.isLoading = false;
        return Promise.reject(err);
      }
      
      if (err.response && err.response.headers && err.response.headers.message == 'not login') {
        // 只有在有 token 的情况下才清除登录状态
        const hasToken = localStorage.getItem("teri_token");
        if (hasToken) {
          // 修改当前的登录状态
          userStore.initData();
          // 关闭websocket
          if (userStore.ws) {
            userStore.ws.close();
            userStore.ws = null;
          }
          // 清除本地token缓存
          localStorage.removeItem("teri_token");
          ElMessage.error("登录已过期，请重新登录");
        }
        userStore.isLoading = false;
      } else {
        // Only show error for non-404 errors
        if (!err.response || err.response.status !== 404) {
          ElMessage.error("特丽丽被玩坏了(¯﹃¯)");
        }
        userStore.isLoading = false;
      }
      return Promise.reject(err);
    },
  );

  instance.defaults.withCredentials = true;

  // axios.post("http://xxx/xxx",
  //   //参数列表
  //   { 'id': id },
  //   //请求头配置   
  //   { headers: {'token':token }}
  // )

  // 如果 data 是 Content-Type: application/json ，后端要用 @RequestBody 接收
  if (headers) {
    return instance.post(url, data, headers); // 有请求头
  }
  return instance.post(url, data);  // 没请求头
}
