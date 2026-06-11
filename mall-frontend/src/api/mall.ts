import { request } from './http';
import type {
  Address,
  Cart,
  CreateOrderPayload,
  CreateOrderResult,
  LoginResponse,
  PayCreateResult,
  UnknownRecord,
  UserInfo,
} from './types';

export const mallApi = {
  login(payload: { username: string; password: string; loginType?: string }) {
    return request<LoginResponse>({
      method: 'POST',
      url: '/api/v1/auth/login',
      data: payload,
    });
  },
  logout() {
    return request<void>({
      method: 'POST',
      url: '/api/v1/auth/logout',
    });
  },
  register(payload: { username: string; phone: string; password: string }) {
    return request<void>({
      method: 'POST',
      url: '/api/v1/users/register',
      data: payload,
    });
  },
  currentUser() {
    return request<UserInfo>({
      method: 'GET',
      url: '/api/v1/users/me',
    });
  },
  updateUser(payload: Partial<UserInfo>) {
    return request<void>({
      method: 'PUT',
      url: '/api/v1/users/me',
      data: payload,
    });
  },
  listAddresses() {
    return request<Address[]>({
      method: 'GET',
      url: '/api/v1/users/me/addresses',
    });
  },
  addAddress(payload: Address) {
    return request<void>({
      method: 'POST',
      url: '/api/v1/users/me/addresses',
      data: payload,
    });
  },
  categories() {
    return request<UnknownRecord[]>({
      method: 'GET',
      url: '/api/v1/categories/tree',
      silent: true,
    });
  },
  product(spuId: number) {
    return request<UnknownRecord>({
      method: 'GET',
      url: `/api/v1/products/${spuId}`,
      silent: true,
    });
  },
  inventoryStock(skuId: number) {
    return request<UnknownRecord>({
      method: 'GET',
      url: `/api/v1/inventory/stock/${skuId}`,
      silent: true,
    });
  },
  searchProducts(keyword: string, pageNum = 1, pageSize = 10) {
    return request<UnknownRecord>({
      method: 'GET',
      url: '/api/v1/search/products',
      params: { keyword, pageNum, pageSize },
    });
  },
  hotWords() {
    return request<string[]>({
      method: 'GET',
      url: '/api/v1/search/hot-words',
      silent: true,
    });
  },
  addCart(payload: { skuId: number; quantity: number }) {
    return request<void>({
      method: 'POST',
      url: '/api/v1/carts',
      data: payload,
    });
  },
  cart() {
    return request<Cart>({
      method: 'GET',
      url: '/api/v1/carts',
    });
  },
  updateCartQuantity(skuId: number, quantity: number) {
    return request<void>({
      method: 'PUT',
      url: `/api/v1/carts/${skuId}`,
      data: { quantity },
    });
  },
  updateCartSelected(skuId: number, selected: boolean) {
    return request<void>({
      method: 'PATCH',
      url: `/api/v1/carts/${skuId}`,
      data: { selected },
    });
  },
  deleteCartItem(skuId: number) {
    return request<void>({
      method: 'DELETE',
      url: `/api/v1/carts/${skuId}`,
    });
  },
  createOrder(payload: CreateOrderPayload) {
    return request<CreateOrderResult>({
      method: 'POST',
      url: '/api/v1/orders',
      data: payload,
    });
  },
  order(orderNo: string) {
    return request<UnknownRecord>({
      method: 'GET',
      url: `/api/v1/orders/${orderNo}`,
    });
  },
  createPay(orderNo: string, payChannel = 'ALIPAY') {
    return request<PayCreateResult>({
      method: 'POST',
      url: '/api/v1/pay/create',
      data: { orderNo, payChannel },
    });
  },
  notifyPay(orderNo: string) {
    return request<string>({
      method: 'POST',
      url: '/api/v1/pay/notify',
      params: {
        out_trade_no: orderNo,
        trade_no: `LOCAL-${Date.now()}`,
        trade_status: 'TRADE_SUCCESS',
      },
    });
  },
  payRecord(orderNo: string) {
    return request<UnknownRecord>({
      method: 'GET',
      url: `/api/v1/pay/record/${orderNo}`,
    });
  },
  seckillActivities() {
    return request<UnknownRecord[]>({
      method: 'GET',
      url: '/api/v1/seckill/activities',
    });
  },
  seckillActivity(activityId: number) {
    return request<UnknownRecord>({
      method: 'GET',
      url: `/api/v1/seckill/activities/${activityId}`,
    });
  },
  createSeckill(activityId: number, skuId: number, quantity = 1) {
    return request<UnknownRecord>({
      method: 'POST',
      url: `/api/v1/seckill/${activityId}`,
      data: { skuId, quantity },
    });
  },
  seckillResult(requestId: string) {
    return request<UnknownRecord>({
      method: 'GET',
      url: `/api/v1/seckill/result/${requestId}`,
    });
  },
  adminDashboard() {
    return request<UnknownRecord>({
      method: 'GET',
      url: '/api/v1/admin/dashboard',
    });
  },
  adminOrders(pageNum = 1, pageSize = 10) {
    return request<UnknownRecord>({
      method: 'GET',
      url: '/api/v1/admin/orders',
      params: { pageNum, pageSize },
    });
  },
  adminProducts(pageNum = 1, pageSize = 10) {
    return request<UnknownRecord>({
      method: 'GET',
      url: '/api/v1/admin/products',
      params: { pageNum, pageSize },
    });
  },
  shipOrder(orderNo: string, company = 'LOCAL', trackingNo = `TRACK-${Date.now()}`) {
    return request<void>({
      method: 'POST',
      url: `/api/v1/admin/orders/${orderNo}/ship`,
      data: { company, trackingNo },
    });
  },
};
