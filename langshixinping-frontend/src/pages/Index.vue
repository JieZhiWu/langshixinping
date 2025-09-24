<script setup lang="ts">
import myAxios from "../plugins/myAxios.js";
import UserCardList from "../components/UserCardList.vue";
import {onMounted,computed,ref, watchEffect} from "vue";
import {showFailToast} from "vant";
import {getCurrentUser} from "../services/user";

// 匹配模式开关状态
const isMatchMode = ref<boolean>(false);
// 用户列表数据
const userList = ref([]);
// 加载状态
const loading = ref(true);
// 登录状态（新增：用于控制开关显示）
const isLogin = ref<boolean>(false);

// 获取推荐标题文本
const titleText = computed(() => {
  return isMatchMode.value
      ? "灵魂的同色焰" : "海浪交错的沫";
});

// 普通模式获取用户推荐
const fetchUserInfo = async () => {
  try {
    const response = await myAxios.get('/user/recommend', {
      // params: { pageSize: 8, pageNum: 1 },
      skipAuth: true,
    });
    console.log('/user/recommend succeed', response);

    // 1. 提取分页中的用户列表（records）
    const userListData = response?.data/*?.records*/ || [];

    // 2. 新增：解析 tags（和 loadData 逻辑一致）
    userListData.forEach(user => {
      if (user.tags) {
        try {
          user.tags = JSON.parse(user.tags); // 字符串转数组
        } catch (e) {
          console.error('解析标签失败', e);
          user.tags = [];
        }
      }
    });

    return userListData; // 返回解析后的用户列表
  } catch (error) {
    console.error('/user/recommend error', error);
    showFailToast('请求失败');
    return [];
  }
};

// 加载匹配用户数据
const loadData = async () => {
  // 未登录时直接返回，避免无效请求
  if (!isLogin.value) return;

  loading.value = true;
  try {
    let response;
    if (isMatchMode.value) {
      // 匹配模式：携带滑块值（期望匹配的标签数量）
      response = await myAxios.get('/user/match', {
        withCredentials: true,
        params: {
          // pageSize: 8,
          // pageNum: 1,
          num: 8,
          rate: 0.9
        },
      })
    } else {
      // 普通模式：35%相似度匹配
      response = await myAxios.get('/user/match', {
        withCredentials: true,
        params: {
          // pageSize: 8,
          // pageNum: 1,
          num: 8,
          rate: 0.35
        },
      });
    }

    const userListData = response?.data/*?.records*/ || [];
    // 解析标签数据
    userListData.forEach(user => {
      if (user.tags) {
        try {
          user.tags = JSON.parse(user.tags);
        } catch (e) {
          console.error('解析标签失败', e);
          user.tags = [];
        }
      }
    });
    userList.value = userListData;
  } catch (error) {
    console.log('/user/match error', error);
    showFailToast('请求失败');
  } finally {
    loading.value = false;
  }
};

onMounted(async () => { // 关键：给 onMounted 加 async
  // 等待 getCurrentUser() 异步结果，再判断
  const currentUser = await getCurrentUser();
  isLogin.value = !!currentUser; // 转换为布尔值（true=已登录，false=未登录）
  loading.value = true; // 1. 开始加载，设为 true
  try {
    if (!isLogin.value) {
      userList.value = await fetchUserInfo(); // 获取 recomment 数据
    }
  } catch (error) {
    showFailToast('数据加载失败');
  } finally {
    loading.value = false; // 2. 无论成功失败，最后设为 false
  }
});

// 监听匹配模式变化，重新加载数据
watchEffect( () => {
    loadData();
});
</script>

<template>
  <!-- 匹配模式开关 -->
  <van-cell center :title="titleText" v-if="isLogin">
    <template #right-icon>
      <van-switch v-model="isMatchMode" size="24"/>
    </template>
  </van-cell>

  <!-- 用户列表展示 -->
  <user-card-list :user-list="userList" :loading="loading"/>
  <van-empty v-if="!loading && (!userList || userList.length < 1)" description="暂无匹配用户"/>
</template>

<style scoped>
</style>