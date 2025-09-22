<script setup lang="ts">

import {onMounted, ref} from "vue";
import {useRoute} from "vue-router";
import {showSuccessToast, showFailToast} from "vant";
import myAxios from "../plugins/myAxios.js";

import qs from 'qs'

const route = useRoute();

const {tags} = route.query;
const userList = ref([]);//存放用户列表

// 使用钩子函数
onMounted(async () => {//异步调用
  // 为给定 ID 的 user 创建请求
  const userListData = await myAxios.get('/user/search/tags', {
    withCredentials: false,
    params: {
      tagNameList: tags
    },
    //序列化
    paramsSerializer: {
      serialize: params => qs.stringify(params, {indices: false}),
    },
  })
      .then(function (response) {
        console.log('/user/search/tags succeed', response);
        return response?.data; //返回数据  ?.可选链操作符，避免数据为null或undefined时报错
      })
      .catch(function (error) {
        console.log('/user/search/tags error', error);
        showFailToast('请求失败');
      });
  if (userListData) {
    userList.value = userListData;
  }
})
</script>
<!--userListData.forEach(user => {-->
<!--if (user.tags) {-->
<!--user.tags = JSON.parse(user.tags);-->
<!--}-->
<!--})-->

<template>
  <user-card-list :user-list="userList"/>
  <van-empty v-if="!userList || userList.length < 1" description="搜索结果为空"/>
</template>

<style scoped>

</style>