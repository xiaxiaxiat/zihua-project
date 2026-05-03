<script setup>
import { computed, onMounted, ref } from 'vue';
import { getCharacters } from '../api/character';
import http from '../api/http';
import CharacterCard from '../components/CharacterCard.vue';

const helloMessage = ref('正在连接后端...');
const helloError = ref(false);

const characters = ref([]);
const charactersLoading = ref(true);
const charactersError = ref(false);
const searchKeyword = ref('');

const normalizeSearchText = (value) => {
  return String(value || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim();
};

const normalizedKeyword = computed(() => normalizeSearchText(searchKeyword.value));

const filteredCharacters = computed(() => {
  if (!normalizedKeyword.value) {
    return characters.value;
  }

  return characters.value.filter((item) => {
    const searchableFields = [
      item.character,
      item.pinyin,
      item.origin,
      item.culture,
      item.cardMeta?.summary
    ];

    return searchableFields.some((field) => normalizeSearchText(field).includes(normalizedKeyword.value));
  });
});

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
        选择一个汉字，查看它从甲骨文到楷书的演变，播放笔顺动画，并继续生成专属文化字卡。
      </p>
    </section>

    <section class="panel">
      <div class="section-heading">
        <span class="section-dot"></span>
        <h3>精选汉字</h3>
      </div>

      <div v-if="!charactersLoading && !charactersError" class="search-toolbar">
        <label class="search-field" for="character-search">
          <span class="search-label">搜索汉字</span>
          <input
            id="character-search"
            v-model.trim="searchKeyword"
            type="text"
            class="search-input"
            placeholder="可按汉字、拼音、造字法、文化寓意搜索"
          />
        </label>
        <div class="search-stats">
          <p class="stats-text stats-total">已收录 {{ characters.length }} 个精选汉字</p>
          <p class="stats-text">当前显示 {{ filteredCharacters.length }} 个汉字</p>
        </div>
      </div>

      <p v-if="charactersLoading" class="status-text">正在加载字库……</p>
      <p v-else-if="charactersError" class="status-text error">字库加载失败，请检查后端是否启动</p>
      <template v-else>
        <div v-if="filteredCharacters.length === 0" class="empty-state">
          <p class="empty-mark">未命中</p>
          <p class="status-text empty-text">未找到匹配的汉字，请换个关键词试试。</p>
        </div>
        <div v-else class="card-grid">
          <CharacterCard
            v-for="item in filteredCharacters"
            :key="item.id"
            :item="item"
          />
        </div>
      </template>
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
  padding: 48px 24px 72px;
}

.hero,
.panel {
  width: min(100%, 1080px);
  margin: 0 auto;
}

.hero {
  margin-bottom: 28px;
  padding: 42px 36px;
  border: 1px solid #d7c6aa;
  border-radius: 18px;
  background:
    radial-gradient(circle at 88% 12%, rgba(183, 65, 46, 0.08), transparent 24%),
    linear-gradient(180deg, rgba(255, 252, 246, 0.98), rgba(247, 239, 225, 0.92));
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
  letter-spacing: 0;
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
  margin-top: 22px;
  padding: 30px;
  border: 1px solid #decfb5;
  border-radius: 18px;
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

.section-heading h3,
.stats-text,
.status-text,
.search-label {
  margin: 0;
}

.section-heading h3 {
  font-size: 22px;
  font-weight: 600;
  color: #241d18;
}

.search-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(230px, 300px);
  gap: 20px;
  margin-bottom: 24px;
  align-items: end;
}

.search-field {
  display: block;
}

.search-label {
  display: block;
  margin-bottom: 10px;
  color: #7b6758;
  font-size: 14px;
}

.search-input {
  width: 100%;
  min-height: 52px;
  padding: 0 18px;
  border: 1px solid #decfb5;
  border-radius: 14px;
  background-color: #fffdf8;
  color: #2f2720;
  font: inherit;
}

.search-input:focus {
  outline: none;
  border-color: #c4563f;
  box-shadow: 0 0 0 3px rgba(196, 86, 63, 0.12);
}

.search-stats {
  display: grid;
  gap: 6px;
  padding: 14px 18px;
  border: 1px solid #e1d2bb;
  border-radius: 14px;
  background-color: #fffdf8;
}

.stats-text {
  color: #5a4c41;
  font-size: 14px;
  line-height: 1.7;
}

.stats-total {
  color: #2f2720;
  font-weight: 600;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  align-items: stretch;
}

.status-panel {
  margin-top: 24px;
}

.status-text {
  color: #2f2720;
  font-size: 18px;
  line-height: 1.7;
}

.empty-state {
  display: grid;
  place-items: center;
  gap: 8px;
  padding: 28px 18px;
  border: 1px dashed #dcc9ab;
  border-radius: 16px;
  background:
    linear-gradient(180deg, rgba(255, 253, 248, 0.96), rgba(249, 241, 230, 0.88));
}

.empty-mark {
  margin: 0;
  color: #b7412e;
  font-size: 15px;
  font-weight: 600;
}

.empty-text {
  color: #6b5d51;
  text-align: center;
}

.error {
  color: #b23b2a;
}

@media (max-width: 900px) {
  .search-toolbar,
  .card-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .home {
    padding: 28px 14px 48px;
  }

  .hero,
  .panel {
    padding: 24px 16px;
    border-radius: 16px;
  }

  .hero-description,
  .status-text {
    font-size: 16px;
  }

  .card-grid {
    grid-template-columns: minmax(0, 1fr);
    gap: 14px;
  }
}
</style>
