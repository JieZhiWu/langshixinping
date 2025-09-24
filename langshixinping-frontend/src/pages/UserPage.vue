<script setup lang="ts">
import {useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import {getCurrentUser} from "../services/user";
import {showSuccessToast, showFailToast, showToast, Toast} from "vant";
import myAxios from "../plugins/myAxios.js";
import {userGenderEnum} from "../constants/user";
import {UserType} from "../models/user";


const user = ref<UserType[]>([]);
const router = useRouter();

// 方法：获取用户信息
const fetchUserInfo = async () => {
  const res = await myAxios.get('/user/current');
  if (res.code === 0) {
    user.value = res.data;
  }
};

// 1. 组件挂载时获取数据
onMounted(async () => {
  // 获取用户信息
  const currentUser = await getCurrentUser();

  if (!currentUser) {
    showFailToast('请先登录');
    return router.push('/user/login');
  }
  fetchUserInfo();
});


// onMounted(async () =>{
//   user.value = await getCurrentUser();
// if(res.code === 0){
//   user.value = res.data;
//   showSuccessToast('获取用户信息成功');
// }else {
//   showFailToast('获取用户信息失败');
// }
// })

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
  <van-cell title="昵称" is-link to="/user/edit" :value="user.username"
            @click="toEdit('username', '昵称', user.username)"/>
  <van-cell title="账号" :value="user.userAccount"/>
  <van-cell title="头像" is-link to="/user/edit">
    <img style="height: 48px" :src="user.avatarUrl">
  </van-cell>
  <van-cell title="性别" is-link to="/user/edit" :value="userGenderEnum[user.gender]"
            @click="toEdit('gender', '性别', userGenderEnum[user.gender])"/>
  <van-cell title="电话" is-link to="/user/edit" :value="user.phone" @click="toEdit('phone', '电话', user.phone)"/>
  <van-cell title="邮箱" is-link to="/user/edit" :value="user.email" @click="toEdit('email', '邮箱', user.email)"/>
  <van-cell title="‘梦' 游编号" :value="user.planetCode"/>
  <van-cell title="更多信息" is-link to="/user/update"/>
  <van-divider/>

  <!--  <van-cell title="修改用户信息" is-link to="/user/update" />-->
  <van-cell title="我创建的队伍" is-link to="/team/create"/>
  <van-cell title="我加入的队伍" is-link to="/team/join"/>
</template>

<style scoped>

</style>