<script setup lang="ts">
import { ref } from 'vue';
import {useRoute, useRouter} from "vue-router";
import myAxios from "../plugins/myAxios.js";
import {showSuccessToast, showFailToast} from "vant";

const router = useRouter();
const route = useRoute();
const userAccount = ref('');
const userPassword = ref('');
// 初始化一个空对象，避免 undefined
const user = ref({}); // 或根据接口返回结构初始化，如 { username: '', avatar: '' }

const onSubmit = async () => {
  const res = await myAxios.post("/user/login", {
    userAccount: userAccount.value,
    userPassword: userPassword.value
  });
  if (res.code == 0 && res.data != null) {
    showSuccessToast("登录成功");
    // 跳转到用户页面
    const redirectUrl = route.query?.redirect as  string ?? '/';
    window.location.href = redirectUrl;
    // router.replace("/user")
  } else {
    showFailToast("登录失败");
  }
};


</script>

<template>
  <van-form @submit="onSubmit">
    <van-cell-group inset>
      <van-field
          v-model="userAccount"
          name="userAccount"
          label="账号"
          placeholder="请输入账号"
          :rules="[{ required: true, message: '请填写用户名' }]"
      />
      <van-field
          v-model="userPassword"
          type="password"
          name="userPassword"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请填写密码' }]"
      />
    </van-cell-group>
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        提交
      </van-button>
    </div>
  </van-form>

</template>

<style scoped>

</style>