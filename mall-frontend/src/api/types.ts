export interface Result<T> {
  code: number;
  message: string;
  data: T;
  traceId?: string;
  timestamp?: number;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  userInfo: UserInfo;
}

export interface UserInfo {
  id?: number;
  userId?: number;
  username?: string;
  nickname?: string;
  avatar?: string;
  email?: string;
  phone?: string;
  role?: string;
  roles?: string[] | string;
  status?: number;
}

export interface Address {
  id?: number;
  receiver: string;
  phone: string;
  province: string;
  city: string;
  district: string;
  detail: string;
  isDefault?: boolean;
}

export interface CartItem {
  skuId: number;
  skuName?: string;
  skuImage?: string;
  price?: number;
  quantity: number;
  selected?: boolean;
  subtotal?: number;
}

export interface Cart {
  items: CartItem[];
  totalQuantity: number;
  totalAmount: number;
}

export interface OrderItemInput {
  skuId: number;
  quantity: number;
}

export interface CreateOrderPayload {
  addressId: number;
  items: OrderItemInput[];
  remark?: string;
}

export interface CreateOrderResult {
  orderNo: string;
  totalAmount?: number;
  payUrl?: string;
  expireTime?: number;
}

export interface PayCreateResult {
  orderNo?: string;
  payNo?: string;
  payUrl?: string;
  payForm?: string;
}

export type UnknownRecord = Record<string, unknown>;
