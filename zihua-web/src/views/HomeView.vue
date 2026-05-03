<script setup>
import { onMounted, ref } from 'vue';
import http from '../api/http';

const message = ref('正在连接后端...');
const isError = ref(false);

const fetchHello = async () => {
  try {
    const response = await http.get('/hello');
    message.value = response.data;
    isError.value = false;
  } catch (error) {
    message.value = '接口连接失败，请检查后端是否启动';
    isError.value = true;
  }
};

onMounted(() => {
  fetchHello();
});
</script>

<template>
  <main class="home">
    <h1>字活</h1>
    <p :class="{ error: isError }">{{ message }}</p>
  </main>
</template>

<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background-color: #ffffff;
  font-family: "Microsoft YaHei", sans-serif;
}

h1 {
  margin: 0;
  font-size: 40px;
  font-weight: 600;
}

p {
  margin: 0;
  font-size: 18px;
  color: #333333;
}

.error {
  color: #c0392b;
}
</style>
