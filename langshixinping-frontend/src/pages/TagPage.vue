<script setup>
import {ref, computed} from 'vue';
import {useRoute, useRouter} from "vue-router";
import {getCurrentUser} from "../services/user.js";
import {showFailToast, showSuccessToast} from "vant";
import myAxios from "../plugins/myAxios.js";

// 搜索关键词
const searchText = ref('');

// 原始标签分组（分类 + 标签列表）
const tagGroups = ref([
  {
    category: '方向',
    tags: [
      {text: '诗歌', id: 'poetry'},
      {text: '散文', id: 'prose'},
      {text: '小说', id: 'novel'},
      {text: '哲学', id: 'philosophy'},
    ],
  },
  {
    category: '喜欢看',
    tags: [
      {text: '中国文学', id: 'chineseLiterature'},
      {text: '外国文学', id: 'foreignLiterature'},
      {text: '现代诗歌', id: 'modernPoetry'},
      {text: '中国诗词', id: 'chinesePoetry'},
      {text: '现实主义', id: 'realism'},
      {text: '浪漫主义', id: 'romanticism'},
    ],
  },
  {
    category: '段位',
    tags: [
      {text: '入门', id: 'beginner'},
      {text: '略知一二', id: 'slightKnowledge'},
      {text: '小有成就', id: 'someAchievement'},
    ],
  },
  {
    category: '身份',
    tags: [
      {text: '小学', id: 'primarySchool'},
      {text: '初中', id: 'juniorHigh'},
      {text: '高中', id: 'seniorHigh'},
      {text: '大学', id: 'university'},
      {text: '学生', id: 'student'},
      {text: '待业', id: 'unemployed'},
      {text: '已就业', id: 'employed'},
      {text: '研究生', id: 'postgraduate'},
    ],
  },
  {
    category: '状态',
    tags: [
      {text: '乐观', id: 'optimistic'},
      {text: '低迷', id: 'lowSpirits'},
      {text: '平淡', id: 'plain'},
      {text: '单身', id: 'single'},
      {text: '已婚', id: 'married'},
      {text: '恋爱', id: 'inLove'},
    ],
  },
  {
    category: '其他标签',
    tags: [
      {text: '男', id: 'male'},
      {text: '女', id: 'female'},
    ],
  },
]);

const router = useRouter();
const route = useRoute();
// 从路由查询参数中获取编辑相关参数
// const editUser = ref({
//   editKey: route.query.editKey,
//   currentValue: route.query.currentValue,
// });
const editUser = ref({
  editKey: route.query.editKey,
  currentValue: route.query.currentValue,
  editName: route.query.editName
});

// 已选中的标签列表
const selectedTags = ref([]);

// 搜索过滤后的标签组（计算属性，根据searchText动态过滤）
const filteredTagGroups = computed(() => {
  if (!searchText.value) {
    return tagGroups.value;
  }
  return tagGroups.value.map((group) => {
    const filteredTags = group.tags.filter((tag) =>
        tag.text.includes(searchText.value)
    );
    return {...group, tags: filteredTags};
  });
});

// 选择标签
const selectTag = (tag) => {
  // 若已选中则取消，否则添加
  const isSelected = selectedTags.value.some((t) => t.id === tag.id);
  if (isSelected) {
    selectedTags.value = selectedTags.value.filter((t) => t.id !== tag.id);
  } else {
    selectedTags.value.push(tag);
  }
};

// 移除已选标签
const removeTag = (tagId) => {
  selectedTags.value = selectedTags.value.filter((t) => t.id !== tagId);
};

// 搜索事件：触发计算属性更新
const onSearch = () => {
  // 无需额外逻辑，computed会自动根据searchText更新
};

// 取消搜索：清空关键词，恢复所有标签
const onCancel = () => {
  searchText.value = '';
};

// 取消按钮：重置所有标签为未选中
const clearTags = () => {
  selectedTags.value = [];
};

const confirm = async () => {
  const currentUser = await getCurrentUser();
  if (!currentUser) {
    showFailToast('更新失败，请先登录');
    return router.push('/login');
  }
  if (!selectedTags.value.length) {
    showFailToast("请选择标签后再提交");
    return;
  }

  // 标签字段：始终发送
  const submitValue = JSON.stringify(selectedTags.value.map(tag => tag.text));

  const res = await myAxios.post('/user/update', {
    id: currentUser.id,
    tags: submitValue
  });

  console.log(res, '更新请求');
  if (res && res?.data > 0) {
    showSuccessToast('更新成功');
    router.back();
  } else {
    showFailToast('更新失败');
  }
};

/*
const confirm = async () => {
  const currentUser = await getCurrentUser();
  if (!currentUser) {
    showFailToast('更新失败，请先登录');
    return router.push('/login');
  }
  if (!selectedTags.value.length) {
    showToast("请选择标签后再提交");
    return;
  }

  // 处理标签数组为字符串（示例：若编辑的是 tags 字段）
  let submitValue = editUser.value.currentValue;
  if (editUser.value.editKey === 'tags') {
    submitValue = JSON.stringify(selectedTags.value.map(tag => tag.text));
  }

  const res = await myAxios.post('/user/update', {
    id: currentUser.id,
    [editUser.value.editKey]: submitValue
  });

  // 后续成功/失败提示...
  console.log(res, '更新请求');
  if (res && res > 0) {
    showSuccessToast('更新成功');
    router.back();
  } else {
    showFailToast('更新失败');
  }

};
*/
</script>

<template>
  <div class="search-page">
    <form action="/">
      <van-search
          v-model="searchText"
          show-action
          shape="round"
          placeholder="请输入搜索标签"
          @search="onSearch"
          @cancel="onCancel"
      />
    </form>

    <!-- 已选标签 -->
    <van-divider v-if="selectedTags.length > 0">已选标签</van-divider>
    <div class="selected-tags-container" v-if="selectedTags.length > 0">
      <van-tag
          v-for="tag in selectedTags"
          :key="tag.id"
          class="custom-tag"
          closeable
          size="small"
          type="primary"
          @close="removeTag(tag.id)"
      >
        {{ tag.text }}
      </van-tag>
    </div>

    <!-- 请选择标签提示（无选中时显示） -->
    <van-divider v-else>请选择标签</van-divider>

    <!-- 平铺标签组 -->
    <div v-for="group in filteredTagGroups" :key="group.category">
      <van-cell :title="group.category" :border="false"/>
      <div class="tag-group">
        <van-tag class="custom-tag"
                 v-for="tag in group.tags"
                 :key="tag.id"
                 :type="selectedTags.some(t => t.id === tag.id) ? 'primary' : 'default'"
                 @click="selectTag(tag)"
        >
          {{ tag.text }}
        </van-tag>
      </div>
    </div>
    <!-- 操作按钮 -->
    <div class="btn-group">
      <van-button type="default" @click="clearTags">清空</van-button>
      <van-button type="primary" @click="confirm">确认</van-button>
    </div>
  </div>
</template>

<style scoped>
/* 已选标签容器：Flex 换行 + 间距控制 */
.selected-tags-container {
  display: flex;
  flex-wrap: wrap; /* 自动换行 */
  gap: 8px; /* 标签之间的间距（水平+垂直） */
  padding: 0 16px; /* 左右内边距，和下方标签组对齐 */
  align-items: flex-start; /* 垂直方向顶部对齐 */
}

/* 标签组容器，设置flex换行和间距 */
.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px; /* 标签之间的间距 */
  padding: 0 16px 16px;
}

/* 自定义标签样式，设置圆角边框 */
.custom-tag {
  border-radius: 20px !important; /* 圆角，数值可根据需要调整 */
  margin: 5px 0; /* 上下微小间距，可根据需要调整 */
  padding: 3px 8px; /* 左右内边距，可选 */
}

/* 搜索页面容器，设置最大高度和滚动 */
.search-page {
  max-height: calc(100vh - 100px); /* 计算可用高度，如底部导航高 50px，则写 calc(100vh - 50px) */
  overflow-y: auto; /* 内容超出时垂直滚动 */
  padding: 0 16px; /* 可选：添加内边距 */
  box-sizing: border-box; /* 避免padding撑大容器 */
  align-items: flex-start; /* 垂直方向顶部对齐 */
}

/* 按钮容器：固定在底部，宽度100%，Flex布局控制间距 */
/* 按钮容器：固定底部 + 居中 + 层次区分 */
.btn-group {
  position: fixed;
  bottom: 0;
  left: -12px;
  width: 100%;
  padding: 16px 24px; /* 更宽松的左右内边距 */
  box-sizing: border-box;
  background-color: #fff;
  border-top: 1px solid #eee; /* 上边框区分内容 */
  display: flex;
  justify-content: center; /* 按钮水平居中 */
  gap: 16px; /* 按钮间距更紧凑协调 */
  z-index: 999;
}

</style>