<script setup lang="ts">
import {ref} from 'vue';
import {showSuccessToast, showFailToast, showToast, Toast} from 'vant';
import {useRouter} from 'vue-router';
import routes from "../config/router";

const router = useRouter();

const DEFAULT_TITLE = '浪矢心瓶';
const title = ref(DEFAULT_TITLE);

/**
 * 根据路由设置标题
 */
router.beforeEach((to, from) => {
  const toPath = to.path;
  const route = routes.find((route)=>{
    return route.path === toPath;
  })
  title.value = route?.title ?? DEFAULT_TITLE;
});

const onClickLeft = () => {
  router.back();
};
const onClickRight = () => {
  router.push('/search');
};
// const active = ref("index");
// const onChange = (index: number) => showSuccessToast(`标签 ${index}`);
</script>

<template>
  <van-nav-bar
      :title="title"
      left-arrow
      @click-left="onClickLeft"
      @click-right="onClickRight"
  >
    <template #right>
      <van-icon name="search" size="18"/>
    </template>
  </van-nav-bar>
  <div id="content">
    <router-view/>
  </div>
  <van-tabbar route>
    <van-tabbar-item to="/" icon="home-o" name="index">主页</van-tabbar-item>
    <van-tabbar-item to="/team" icon="friends-o" name="team">队伍</van-tabbar-item>
    <van-tabbar-item to="/user" icon="setting-o" name="user">个人</van-tabbar-item>
  </van-tabbar>

</template>

<style scoped>
#content {
  padding-bottom: 50px;
}
</style>