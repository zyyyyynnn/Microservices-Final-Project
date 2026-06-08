import axios, { AxiosError, type AxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';
import type { Result } from './types';

export class ApiError extends Error {
  code: number;

  constructor(message: string, code = 0) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
  }
}

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000,
});

interface MallRequestConfig extends AxiosRequestConfig {
  silent?: boolean;
}

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('mallcloud_access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    const body = response.data;
    if (typeof body === 'string') {
      return body;
    }
    if (body && typeof body.code === 'number') {
      const result = body as Result<unknown>;
      if (result.code !== 200) {
        throw new ApiError(result.message || '接口请求失败', result.code);
      }
      return result.data;
    }
    return body;
  },
  (error: AxiosError<Result<unknown>>) => {
    const status = error.response?.status;
    const message = error.response?.data?.message || error.message || '接口请求失败';
    throw new ApiError(status ? `${status}: ${message}` : message, error.response?.data?.code || status || 0);
  },
);

export async function request<T>(config: MallRequestConfig) {
  const { silent, ...axiosConfig } = config;
  try {
    return await http.request<unknown, T>(axiosConfig);
  } catch (error) {
    const message = error instanceof ApiError ? error.message : '接口请求失败';
    if (!silent) {
      ElMessage.error(message);
    }
    throw error;
  }
}
