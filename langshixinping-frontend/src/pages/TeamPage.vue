<script setup lang="ts">
import {useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios.js";
import TeamCardList from "../components/TeamCardList.vue";

const router = useRouter();

const searchText = ref('');
const active = ref('public');

const tabTitles = {
  public: "公开",
  private: "加密"
};

const doJoinTeam = () => {
  router.push({
    path: "/team/add",
  });
}

/**
 * 切换查询状态
 */
const onTabChange = (name) => {
  // 查公开
  if (name === 'public') {
    listTeam(searchText.value, 0);
  }else {
    // 查询加密
    listTeam(searchText.value, 2);
  }
}

const teamList = ref([]);

// 队伍搜索方法
const listTeam = async (val = '', status = 0) => {
  const res = await myAxios.get("/team/list", {
    params: {
      searchText: val,
      pageNum: 1,
      status,
    }
  });
  if (res?.code === 0) {
    teamList.value = res.data;
  } else {
    showFailToast("加载队伍失败，请刷新")
  }
}

onMounted(async () => {
  listTeam()
})

const onSearch = (val) => {
  listTeam(val);
}

const onClickButton = () => {
  onSearch(searchText.value);
}

</script>


<template>
  <div id="teamPage">
    <van-search v-model="searchText" shape="round" show-action placeholder="搜索队伍"
                @search="onSearch"
    >
      <template #action>
        <div @click="onClickButton">搜索</div>
      </template>
    </van-search>
    <van-tabs v-model:active="active" @change="onTabChange">
      <van-tab :title="tabTitles.public" name="public" />
      <van-tab :title="tabTitles.private" name="private" />
    </van-tabs>
    <div style="margin-bottom:16px" />
    <van-floating-bubble :gap="{ x: 20, y: 60 }" type="primary" class="add-button" icon="plus" @click="doJoinTeam" />
    <team-card-list :teamList="teamList"/>
    <van-empty v-if="teamList.length < 1" description="数据为空" />
  </div>

</template>


<style scoped>
</style>