<script setup lang="ts">
import myAxios from "../plugins/myAxios.js";
import UserCardList from "../components/UserCardList.vue";
import { computed, onMounted, ref, watchEffect } from "vue";
import { showFailToast } from "vant";
import { getCurrentUser } from "../services/user";

// 匹配模式开关状态
const isMatchMode = ref<boolean>(false);
// 用户列表数据
const userList = ref([]);
// 加载状态
const loading = ref(true);

// 获取推荐标题文本
const titleText = computed(() => {
  return isMatchMode.value
      ? "灵魂的同色焰" : "海浪交错的沫";
});

// 普通模式获取用户推荐
const fetchUserInfo = async () => {
  try {
    const response = await myAxios.get('/user/recommend', {
      params: {
        pageSize: 8,
        pageNum: 1,
      },
    });
    console.log('/user/recommend succeed', response);
    return response?.data?.records || [];
  } catch (error) {
    console.error('/user/recommend error', error);
    showFailToast('请求失败');
    return [];
  }
};


// 加载匹配用户数据
const loadData = async () => {
  loading.value = true;
  try {
    let response;
    if (isMatchMode.value) {
      // 匹配模式：携带滑块值（期望匹配的标签数量）
      response = await myAxios.get('/user/match', {
        withCredentials: true,
        params: {
          num: 10,
          type: 0,
          rate: 0.9
        },
      });
    } else {
      // 普通模式：35%相似度匹配
      response = await myAxios.get('/user/match', {
        withCredentials: true,
        params: {
          num: 10,
          type: 1,
          rate: 0.35
        },
      });
    }

    const userListData = response?.data || [];
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

// 监听匹配模式变化，重新加载数据
watchEffect(async () => {
  if (getCurrentUser()!= null){
    await loadData();
  }else {
    userList.value = await fetchUserInfo();
  }
});
</script>

<template>
  <!-- 匹配模式开关 -->
  <van-cell center :title="titleText">
    <template #right-icon>
      <van-switch v-model="isMatchMode" size="24" />
    </template>
  </van-cell>

  <!-- 用户列表展示 -->
  <user-card-list :user-list="userList" :loading="loading" />
  <van-empty v-if="!loading && (!userList || userList.length < 1)" description="暂无匹配用户" />
</template>

<style scoped>
</style>