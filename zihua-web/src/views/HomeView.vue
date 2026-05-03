<script setup>
import { onMounted, ref } from 'vue';
import { getCharacters } from '../api/character';
import http from '../api/http';
import CharacterCard from '../components/CharacterCard.vue';

const helloMessage = ref('正在连接后端...');
const helloError = ref(false);

const characters = ref([]);
const charactersLoading = ref(true);
const charactersError = ref(false);

const fetchHello = async () => {
  try {
    const response = await http.get('/hello');
    helloMessage.value = response.data;
    helloError.value = false;
  } catch (error) {
    helloMessage.value = '接口连接失败，请检查后端是否启动';
    helloError.value = true;
  }
};

const fetchCharacters = async () => {
  charactersLoading.value = true;
  charactersError.value = false;

  try {
    const response = await getCharacters();
    characters.value = response.data;
  } catch (error) {
    characters.value = [];
    charactersError.value = true;
  } finally {
    charactersLoading.value = false;
  }
};

onMounted(() => {
  fetchHello();
  fetchCharacters();
});
</script>

<template>
  <main class="home">
    <section class="hero">
      <p class="hero-mark">字有来处，意有新生</p>
      <h1 class="hero-title">《字活》</h1>
      <h2 class="hero-subtitle">AI 汉字字源演变与互动创作平台</h2>
      <p class="hero-description">
        选择一个汉字，查看它从甲骨文到楷书的演变，播放笔顺动画，生成专属字卡。
      </p>
    </section>

    <section class="panel">
      <div class="section-heading">
        <span class="section-dot"></span>
        <h3>样例汉字</h3>
      </div>

      <p v-if="charactersLoading" class="status-text">正在加载字库……</p>
      <p v-else-if="charactersError" class="status-text error">字库加载失败，请检查后端是否启动</p>
      <div v-else class="card-grid">
        <CharacterCard
          v-for="item in characters"
          :key="item.id"
          :item="item"
        />
      </div>
    </section>

    <section class="panel status-panel">
      <div class="section-heading">
        <span class="section-dot"></span>
        <h3>接口状态</h3>
      </div>
      <p class="status-text" :class="{ error: helloError }">{{ helloMessage }}</p>
    </section>
  </main>
</template>

<style scoped>
.home {
  min-height: 100vh;
  padding: 56px 24px 72px;
}

.hero,
.panel {
  width: min(100%, 1080px);
  margin: 0 auto;
}

.hero {
  margin-bottom: 32px;
  padding: 40px 32px;
  border: 1px solid #d7c6aa;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(255, 252, 246, 0.98), rgba(247, 239, 225, 0.92));
  box-shadow: 0 18px 40px rgba(47, 39, 32, 0.08);
}

.hero-mark {
  margin: 0 0 12px;
  color: #a53a2a;
  font-size: 14px;
  letter-spacing: 1px;
}

.hero-title {
  margin: 0;
  font-size: clamp(40px, 6vw, 64px);
  line-height: 1.1;
  color: #1f1a17;
}

.hero-subtitle {
  margin: 16px 0 0;
  font-size: clamp(20px, 3vw, 28px);
  font-weight: 500;
  color: #2f2720;
}

.hero-description {
  max-width: 720px;
  margin: 18px 0 0;
  font-size: 17px;
  line-height: 1.8;
  color: #5a4c41;
}

.panel {
  margin-top: 24px;
  padding: 28px;
  border: 1px solid #decfb5;
  border-radius: 16px;
  background-color: rgba(255, 252, 246, 0.94);
  box-shadow: 0 14px 30px rgba(58, 44, 34, 0.06);
}

.section-heading {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.section-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background-color: #b7412e;
  flex-shrink: 0;
}

.section-heading h3 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #241d18;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.status-panel {
  margin-top: 24px;
}

.status-text {
  margin: 0;
  color: #2f2720;
  font-size: 18px;
  line-height: 1.7;
}

.error {
  color: #b23b2a;
}

@media (max-width: 900px) {
  .card-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .home {
    padding: 32px 16px 48px;
  }

  .hero,
  .panel {
    padding: 24px 18px;
  }
}
</style>
