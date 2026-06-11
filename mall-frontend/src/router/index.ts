import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import HomeView from '../views/HomeView.vue';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import ProductDetailView from '../views/ProductDetailView.vue';
import SearchView from '../views/SearchView.vue';
import AccountView from '../views/AccountView.vue';
import CartView from '../views/CartView.vue';
import CheckoutView from '../views/CheckoutView.vue';
import OrderDetailView from '../views/OrderDetailView.vue';
import PayView from '../views/PayView.vue';
import SeckillView from '../views/SeckillView.vue';
import AdminView from '../views/AdminView.vue';
import NotFoundView from '../views/NotFoundView.vue';

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/login', name: 'login', component: LoginView },
  { path: '/register', name: 'register', component: RegisterView },
  { path: '/products/:id', name: 'product-detail', component: ProductDetailView },
  { path: '/search', name: 'search', component: SearchView },
  { path: '/account', name: 'account', component: AccountView, meta: { auth: true } },
  { path: '/cart', name: 'cart', component: CartView, meta: { auth: true } },
  { path: '/checkout', name: 'checkout', component: CheckoutView, meta: { auth: true } },
  { path: '/orders/:orderNo', name: 'order-detail', component: OrderDetailView, meta: { auth: true } },
  { path: '/pay/:orderNo', name: 'pay', component: PayView, meta: { auth: true } },
  { path: '/seckill', name: 'seckill', component: SeckillView, meta: { auth: true } },
  { path: '/admin', name: 'admin', component: AdminView, meta: { auth: true } },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFoundView },
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
