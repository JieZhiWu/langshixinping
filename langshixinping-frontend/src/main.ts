import { createApp } from 'vue'
// import './style.css'    留着会导致布局样式冲突
import App from './App.vue'
import {Button, Icon, NavBar, Tabbar, TabbarItem} from 'vant';
import {createRouter, createWebHashHistory} from 'vue-router'
import routes from './config/router'
import * as VueRouter from 'vue-router';
import './global.css'

const app = createApp(App);
// 此处可以不写app.use()按需引入，因为vite-plugin-windicss已经开启了按需引入
app.use(Button);
app.use(NavBar);
app.use(Icon);
app.use(Tabbar);
app.use(TabbarItem);

const router = VueRouter.createRouter({
    // history: VueRouter.createWebHashHistory(),
    history: VueRouter.createWebHistory(),
    routes,
})

app.use(router);

app.mount('#app')
