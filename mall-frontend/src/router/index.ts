import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import HomeView from '../views/HomeView.vue';
import LoginView from '../views/LoginView.vue';
import AccountView from '../views/AccountView.vue';
import CartView from '../views/CartView.vue';
import OrderPayView from '../views/OrderPayView.vue';
import SeckillView from '../views/SeckillView.vue';
import AdminView from '../views/AdminView.vue';
import TechView from '../views/TechView.vue';

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/login', name: 'login', component: LoginView },
  { path: '/account', name: 'account', component: AccountView, meta: { auth: true } },
  { path: '/cart', name: 'cart', component: CartView, meta: { auth: true } },
  { path: '/orders', name: 'orders', component: OrderPayView, meta: { auth: true } },
  { path: '/seckill', name: 'seckill', component: SeckillView, meta: { auth: true } },
  { path: '/admin', name: 'admin', component: AdminView, meta: { auth: true } },
  { path: '/tech', name: 'tech', component: TechView },
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to) => {
  const auth = useAuthStore();
  if (to.meta.auth && !auth.isAuthenticated) {
    return { path: '/login', query: { redirect: to.fullPath } };
  }
  return true;
});
