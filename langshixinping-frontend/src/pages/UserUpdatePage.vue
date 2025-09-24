<script setup lang="ts">
import {useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import {getCurrentUser} from "../services/user";
import {showSuccessToast, showFailToast, showToast, Toast} from "vant";
import myAxios from "../plugins/myAxios.js";
import dayjs from 'dayjs';
import utc from 'dayjs/plugin/utc';
import timezone from 'dayjs/plugin/timezone';
import {UserType} from "../models/user";

// 扩展 Day.js 时区功能
dayjs.extend(utc);
dayjs.extend(timezone);

const user = ref<UserType[]>([]);
const router = useRouter();

// 格式化函数：将 UTC 时间转换为本地时区的 "年-月-日 时:分:秒"
const formattedRegisterTime = dayjs(user.createTime)
    .tz() // 转换为本地时区
    .format('YYYY-MM-DD');

// 方法：获取用户信息
const fetchUserInfo = async () => {
  const res = await myAxios.get('/user/current');
  if (res.code === 0) {
    user.value = res.data;
  }
};

// 1. 组件挂载时获取数据
onMounted( async() => {
  // 获取用户信息
  const currentUser = await getCurrentUser();

  if(!currentUser){
    showFailToast('请先登录');
    return router.push('/user/login');
  }
  fetchUserInfo();
});


const toEdit = (editKey: string, editName: string, currentValue: string) => {
  router.push({
    path: '/user/edit',
    query: {
      editKey,
      editName,
      currentValue,
    }
  })
}
</script>

<template>
<!--  <van-cell title="地区" is-link to="/user/edit" :value="user.username" @click="toEdit('username', '地区', user.username)" />-->
  <van-cell title="简介" is-link to="/user/edit" :value="user.profile" @click="toEdit('profile', '简介', user.profile)" />
  <van-cell title="兴趣标签" is-link to="/user/tag" :value="user.tags" />
  <van-cell title="注册时间" :value="formattedRegisterTime" />
</template>

<style scoped>

</style>