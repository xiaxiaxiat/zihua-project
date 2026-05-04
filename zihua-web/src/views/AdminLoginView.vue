<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const DEMO_USERNAME = 'admin';
const DEMO_PASSWORD = '123456';

const router = useRouter();

const username = ref('');
const password = ref('');
const errorMessage = ref('');
const submitting = ref(false);

const handleSubmit = async () => {
  submitting.value = true;
  errorMessage.value = '';

  const isValid =
    username.value.trim() === DEMO_USERNAME &&
    password.value === DEMO_PASSWORD;

  if (!isValid) {
    errorMessage.value = '账号或密码错误';
    submitting.value = false;
    return;
  }

  localStorage.setItem('adminLoggedIn', 'true');
  router.push('/admin/characters');
};
</script>

<template>
  <main class="login-page">
    <section class="login-shell">
      <div class="login-card">
        <p class="login-mark">管理员入口</p>
        <h1 class="login-title">登录字活后台</h1>
        <p class="login-description">输入演示账号后即可进入管理员端。游客端浏览功能不受影响。</p>

        <form class="login-form" @submit.prevent="handleSubmit">
          <label class="field">
            <span>用户名</span>
            <input v-model.trim="username" type="text" autocomplete="username" placeholder="请输入用户名" />
          </label>

          <label class="field">
            <span>密码</span>
            <input v-model="password" type="password" autocomplete="current-password" placeholder="请输入密码" />
          </label>

          <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

          <div class="action-row">
            <button class="primary-button" type="submit" :disabled="submitting">
              {{ submitting ? '登录中...' : '登录' }}
            </button>
            <router-link class="ghost-button" to="/">返回游客首页</router-link>
          </div>
        </form>

        <div class="demo-tip">
          <p>演示账号：admin</p>
          <p>演示密码：123456</p>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  padding: 36px 20px;
  display: grid;
  place-items: center;
}

.login-shell {
  width: min(100%, 560px);
}

.login-card {
  border: 1px solid #decfb5;
  border-radius: 20px;
  background:
    radial-gradient(circle at 88% 12%, rgba(183, 65, 46, 0.08), transparent 24%),
    rgba(255, 252, 246, 0.96);
  box-shadow: 0 18px 40px rgba(47, 39, 32, 0.08);
  padding: 34px 30px;
}

.login-mark,
.login-title,
.login-description,
.error-text,
.demo-tip p {
  margin: 0;
}

.login-mark {
  color: #a53a2a;
  font-size: 14px;
  letter-spacing: 1px;
}

.login-title {
  margin-top: 10px;
  color: #1f1a17;
  font-size: clamp(30px, 5vw, 42px);
}

.login-description {
  margin-top: 14px;
  color: #5a4c41;
  font-size: 16px;
  line-height: 1.8;
}

.login-form {
  display: grid;
  gap: 18px;
  margin-top: 28px;
}

.field {
  display: grid;
  gap: 10px;
}

.field span {
  color: #7b6758;
  font-size: 14px;
}

.field input {
  width: 100%;
  min-height: 52px;
  padding: 0 18px;
  border: 1px solid #decfb5;
  border-radius: 14px;
  background-color: #fffdf8;
  color: #2f2720;
  font: inherit;
}

.field input:focus {
  outline: none;
  border-color: #c4563f;
  box-shadow: 0 0 0 3px rgba(196, 86, 63, 0.12);
}

.error-text {
  color: #b23b2a;
  font-size: 14px;
}

.action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 6px;
}

.primary-button,
.ghost-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: 0 18px;
  border-radius: 999px;
  font: inherit;
  text-decoration: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.primary-button {
  border: 1px solid #b7412e;
  background-color: #b7412e;
  color: #fffaf4;
  cursor: pointer;
}

.ghost-button {
  border: 1px solid #decfb5;
  background-color: #fffdf8;
  color: #2a221c;
}

.primary-button:hover,
.ghost-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(58, 44, 34, 0.08);
}

.primary-button:disabled {
  cursor: wait;
  opacity: 0.8;
}

.demo-tip {
  margin-top: 26px;
  padding: 16px 18px;
  border: 1px dashed #dcc9ab;
  border-radius: 14px;
  background-color: rgba(255, 253, 248, 0.92);
  color: #5a4c41;
  font-size: 14px;
  line-height: 1.8;
}

@media (max-width: 640px) {
  .login-page {
    padding: 24px 14px;
  }

  .login-card {
    padding: 24px 18px;
    border-radius: 16px;
  }
}
</style>
