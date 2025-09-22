import Index from "../pages/Index.vue";
import TeamPage from "../pages/TeamPage.vue";
import UserPage from "../pages/UserPage.vue";
import SearchPage from "../pages/SearchPage.vue";
import UserEditPage from "../pages/UserEditPage.vue";
import SearchResultPage from "../pages/SearchResultPage.vue";
import UserLoginPage from "../pages/UserLoginPage.vue";
import TeamAddPage from "../pages/TeamAddPage.vue";
import TeamUpdatePage from "../pages/TeamUpdatePage.vue";
import UserUpdatePage from "../pages/UserUpdatePage.vue";
import TeamJoinPage from "../pages/TeamJoinPage.vue";
import TeamCreatePage from "../pages/TeamCreatePage.vue";


const routes = [
    { path: '/', component: Index },
    { path: '/team',title: '找队伍', component: TeamPage },
    { path: '/user',title: '个人信息', component: UserPage },
    { path: '/search',title: '找伙伴', component: SearchPage },
    { path: '/user/edit',title: '编辑信息', component: UserEditPage },
    { path: '/user/list',title: '用户列表', component: SearchResultPage },
    { path: '/user/login',title: '登录', component: UserLoginPage },
    { path: '/user/update/',title: '修改用户信息', component: UserUpdatePage },
    { path: '/team/add',title: '创建队伍', component: TeamAddPage },
    { path: '/team/update',title: '修改队伍信息', component: TeamUpdatePage },
    { path: '/team/join',title: '已加入队伍', component: TeamJoinPage },
    { path: '/team/create',title: '已创建队伍', component: TeamCreatePage },
]

export default routes;