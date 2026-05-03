<script setup>
import { onMounted, ref } from 'vue';
import http from '../api/http';

const message = ref('正在连接后端...');
const isError = ref(false);

const sampleCharacters = [
  {
    character: '山',
    pinyin: 'shan',
    meaning: '稳重如山，象征坚定与生长。'
  },
  {
    character: '家',
    pinyin: 'jia',
    meaning: '屋舍有光，寄托团圆与守护。'
  },
  {
    character: '明',
    pinyin: 'ming',
    meaning: '日月相映，寓意通透与希望。'
  }
];

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
      <div class="card-grid">
        <article
          v-for="item in sampleCharacters"
          :key="item.character"
          class="character-card"
        >
          <div class="card-character">{{ item.character }}</div>
          <div class="card-meta">
            <p class="card-pinyin">{{ item.pinyin }}</p>
            <p class="card-meaning">{{ item.meaning }}</p>
          </div>
        </article>
      </div>
    </section>

    <section class="panel status-panel">
      <div class="section-heading">
        <span class="section-dot"></span>
        <h3>接口状态</h3>
      </div>
      <p class="status-text" :class="{ error: isError }">{{ message }}</p>
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

.character-card {
  display: flex;
  gap: 18px;
  align-items: center;
  min-height: 180px;
  padding: 22px 20px;
  border: 1px solid #e1d2bb;
  border-radius: 14px;
  background-color: #fffdf8;
}

.card-character {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 84px;
  min-width: 84px;
  height: 84px;
  border-radius: 12px;
  background-color: #f3e6d4;
  color: #221a15;
  font-size: 42px;
  font-weight: 600;
}

.card-meta {
  min-width: 0;
}

.card-pinyin {
  margin: 0 0 10px;
  color: #b7412e;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.card-meaning {
  margin: 0;
  color: #53463c;
  font-size: 16px;
  line-height: 1.7;
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

  .character-card {
    min-height: 0;
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

  .character-card {
    align-items: flex-start;
  }

  .card-character {
    width: 72px;
    min-width: 72px;
    height: 72px;
    font-size: 36px;
  }
}
</style>
