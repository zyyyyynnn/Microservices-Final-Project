import { ElMessage } from 'element-plus';

let lastMessage = '';
let lastTime = 0;

export function notifyError(message: string) {
  const now = Date.now();

  if (message === lastMessage && now - lastTime < 1500) {
    return;
  }

  lastMessage = message;
  lastTime = now;

  ElMessage.closeAll();
  ElMessage({
    type: 'error',
    message,
    showClose: true,
    grouping: true,
    placement: 'top',
    duration: 3000,
    customClass: 'mall-global-message',
  });
}

export function notifySuccess(message: string) {
  const now = Date.now();

  if (message === lastMessage && now - lastTime < 1500) {
    return;
  }

  lastMessage = message;
  lastTime = now;

  ElMessage.closeAll();
  ElMessage({
    type: 'success',
    message,
    showClose: true,
    grouping: true,
    placement: 'top',
    duration: 2400,
    customClass: 'mall-global-message',
  });
}
