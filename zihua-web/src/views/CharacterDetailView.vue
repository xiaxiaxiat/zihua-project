<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { getCharacterById } from '../api/character';
import { generateWordAssociation } from '../api/wordAssociation';
import CardExportActions from '../components/CardExportActions.vue';
import CultureCard from '../components/CultureCard.vue';
import EvolutionStageViewer from '../components/EvolutionStageViewer.vue';
import EvolutionTimeline from '../components/EvolutionTimeline.vue';
import StrokeOrderPlayer from '../components/StrokeOrderPlayer.vue';

const route = useRoute();

const detail = ref(null);
const detailLoading = ref(true);
const detailError = ref(false);
const activeStageKey = ref('');

const wordAssociationLoading = ref(false);
const wordAssociationError = ref('');
const wordAssociationResult = ref(null);

const activeStage = computed(() => {
  if (!detail.value?.stages?.length) {
    return null;
  }

  return detail.value.stages.find((stage) => stage.key === activeStageKey.value) || detail.value.stages[0];
});

const cultureCardElementId = computed(() => {
  if (!detail.value?.id) {
    return 'culture-card-preview';
  }

  return `culture-card-${detail.value.id}`;
});

const readErrorMessage = (error, fallback) =>
  error?.response?.data?.message || error?.message || fallback;

const resetDetailState = () => {
  detailLoading.value = true;
  detailError.value = false;
  detail.value = null;
  activeStageKey.value = '';
  wordAssociationLoading.value = false;
  wordAssociationError.value = '';
  wordAssociationResult.value = null;
};

const fetchDetail = async () => {
  resetDetailState();

  try {
    const response = await getCharacterById(route.params.id);
    detail.value = response.data;
    activeStageKey.value = response.data.stages?.[0]?.key || '';
  } catch (error) {
    detailError.value = true;
  } finally {
    detailLoading.value = false;
  }
};

const generateAssociations = async () => {
  if (!detail.value) {
    return;
  }

  wordAssociationLoading.value = true;
  wordAssociationError.value = '';

  try {
    const response = await generateWordAssociation({
      code: detail.value.id,
      character: detail.value.character,
      pinyin: detail.value.pinyin,
      meaning: detail.value.meaning,
      culture: detail.value.culture
    });

    wordAssociationResult.value = response.data;
  } catch (error) {
    wordAssociationError.value = readErrorMessage(
      error,
      'AI 组词生成失败，请稍后重试'
    );
  } finally {
    wordAssociationLoading.value = false;
  }
};

const selectStage = (stageKey) => {
  activeStageKey.value = stageKey;
};

onMounted(() => {
  fetchDetail();
});

watch(
  () => route.params.id,
  () => {
    fetchDetail();
  }
);
</script>

<template>
  <main class="detail-page">
    <section class="detail-shell">
      <router-link to="/" class="back-link">
        <span class="back-arrow">←</span>
        返回首页
      </router-link>

      <section v-if="detailLoading" class="detail-panel">
        <p class="status-text">正在加载汉字详情……</p>
      </section>

      <section v-else-if="detailError" class="detail-panel">
        <p class="status-text error">字库详情加载失败，请检查链接或后端状态。</p>
        <router-link to="/" class="back-button">回到首页</router-link>
      </section>

      <template v-else>
        <section class="detail-hero">
          <div class="hero-left">
            <p class="hero-mark">汉字详情</p>
            <h1 class="hero-character">{{ detail.character }}</h1>
            <div class="hero-meta">
              <span>{{ detail.pinyin }}</span>
              <span>{{ detail.origin }}</span>
            </div>
          </div>
          <div class="hero-right">
            <p class="hero-summary">{{ detail.cardMeta.summary }}</p>
          </div>
        </section>

        <section class="detail-grid">
          <article class="detail-panel compact-panel">
            <div class="section-heading">
              <span class="section-dot"></span>
              <h2>字义与文化</h2>
            </div>
            <div class="info-group">
              <p class="info-label">本义</p>
              <p class="info-text">{{ detail.meaning }}</p>
            </div>
            <div class="info-group">
              <p class="info-label">文化寓意</p>
              <p class="info-text">{{ detail.culture }}</p>
            </div>
          </article>

          <article class="detail-panel compact-panel">
            <div class="section-heading">
              <span class="section-dot"></span>
              <h2>{{ detail.story.title }}</h2>
            </div>
            <p v-if="detail.story && detail.story.reviewed === false" class="review-note">
              该字源故事待人工校对
            </p>
            <p class="info-text">{{ detail.story.body }}</p>
          </article>
        </section>

        <section class="detail-panel">
          <div class="section-heading">
            <span class="section-dot"></span>
            <h2>字源演变时间轴</h2>
          </div>
          <EvolutionTimeline
            :stages="detail.stages"
            :active-stage-key="activeStageKey"
            @select="selectStage"
          />
          <div class="timeline-viewer">
            <EvolutionStageViewer
              v-if="activeStage"
              :stage="activeStage"
              :kaishu-character="detail.kaishuCharacter"
              :character="detail.character"
            />
          </div>
        </section>

        <section class="detail-panel association-panel">
          <div class="association-header">
            <div class="section-heading">
              <span class="section-dot"></span>
              <h2>AI 组词联想</h2>
            </div>
            <button
              type="button"
              class="association-button"
              :disabled="wordAssociationLoading"
              @click="generateAssociations"
            >
              {{ wordAssociationLoading ? 'AI 正在生成组词……' : '生成组词联想' }}
            </button>
          </div>

          <p class="section-intro">
            基于当前汉字生成常见组词和文化联想，帮助你从词语使用中理解这个字的更多表达方式。
          </p>

          <p v-if="wordAssociationError" class="status-text error">{{ wordAssociationError }}</p>

          <div v-if="wordAssociationResult?.words?.length" class="word-grid">
            <article
              v-for="(item, index) in wordAssociationResult.words"
              :key="`${item.word}-${index}`"
              class="word-card"
            >
              <div class="word-head">
                <h3>{{ item.word }}</h3>
                <p v-if="item.pinyin" class="word-pinyin">{{ item.pinyin }}</p>
              </div>
              <div class="word-section">
                <p class="word-label">含义</p>
                <p class="word-text">{{ item.meaning }}</p>
              </div>
              <div class="word-section">
                <p class="word-label">联想</p>
                <p class="word-text">{{ item.usage }}</p>
              </div>
              <div v-if="item.example" class="word-section">
                <p class="word-label">例句</p>
                <p class="word-text">{{ item.example }}</p>
              </div>
            </article>
          </div>
        </section>

        <StrokeOrderPlayer :character="detail.character" />

        <section class="detail-panel card-section">
          <div class="section-heading">
            <span class="section-dot"></span>
            <h2>专属字卡</h2>
          </div>
          <p class="section-intro">
            预览当前汉字的文化字卡，并可一键导出为 PNG 图片。
          </p>
          <div class="culture-card-shell">
            <CultureCard :detail="detail" :card-element-id="cultureCardElementId" />
          </div>
          <CardExportActions
            :card-element-id="cultureCardElementId"
            :character="detail.character"
          />
        </section>
      </template>
    </section>
  </main>
</template>

<style scoped>
.detail-page {
  min-height: 100vh;
  padding: 36px 24px 72px;
}

.detail-shell {
  width: min(100%, 1080px);
  margin: 0 auto;
}

.back-link,
.back-button,
.association-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 42px;
  padding: 0 18px;
  border-radius: 999px;
  font: inherit;
  text-decoration: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.back-link,
.back-button {
  border: 1px solid #decfb5;
  background-color: rgba(255, 252, 246, 0.96);
  color: #2a221c;
}

.association-button {
  border: 1px solid #b7412e;
  background-color: #b7412e;
  color: #fffaf4;
  cursor: pointer;
}

.back-link:hover,
.back-button:hover,
.association-button:hover:not(:disabled) {
  transform: translateY(-1px);
  border-color: #c4563f;
  box-shadow: 0 10px 22px rgba(58, 44, 34, 0.08);
}

.association-button:disabled {
  cursor: wait;
  opacity: 0.75;
}

.back-arrow {
  margin-right: 8px;
  color: #b7412e;
  font-size: 22px;
  line-height: 1;
}

.back-link {
  margin-bottom: 20px;
}

.back-button {
  margin-top: 16px;
}

.detail-hero,
.detail-panel {
  border: 1px solid #decfb5;
  border-radius: 18px;
  background-color: rgba(255, 252, 246, 0.94);
  box-shadow: 0 14px 30px rgba(58, 44, 34, 0.06);
}

.detail-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(260px, 0.8fr);
  gap: 28px;
  padding: 34px;
  background:
    radial-gradient(circle at 90% 12%, rgba(183, 65, 46, 0.08), transparent 26%),
    rgba(255, 252, 246, 0.94);
}

.hero-mark,
.hero-summary,
.info-label,
.info-text,
.status-text,
.section-intro,
.review-note,
.word-label,
.word-text,
.word-pinyin {
  margin: 0;
}

.hero-mark {
  color: #a53a2a;
  font-size: 14px;
  letter-spacing: 1px;
}

.hero-character {
  margin: 0;
  color: #1f1a17;
  font-size: clamp(54px, 10vw, 96px);
  line-height: 1;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 18px;
}

.hero-meta span {
  padding: 8px 14px;
  border-radius: 999px;
  background-color: #f5e7d7;
  color: #5d4f43;
  font-size: 15px;
}

.hero-right {
  display: flex;
  align-items: flex-end;
}

.hero-summary {
  color: #342a23;
  font-size: 20px;
  line-height: 1.8;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px;
  margin-top: 26px;
}

.detail-panel {
  margin-top: 26px;
  padding: 30px;
}

.compact-panel {
  margin-top: 0;
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

.section-heading h2 {
  margin: 0;
  color: #241d18;
  font-size: 24px;
  font-weight: 600;
}

.info-group + .info-group {
  margin-top: 22px;
}

.info-label,
.word-label {
  margin-bottom: 10px;
  color: #b7412e;
  font-size: 14px;
  letter-spacing: 1px;
}

.info-text,
.status-text,
.section-intro,
.word-text {
  color: #4e4136;
  font-size: 16px;
  line-height: 1.9;
}

.review-note {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  margin-bottom: 16px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid rgba(183, 65, 46, 0.16);
  background-color: #fbf0ea;
  color: #a04835;
  font-size: 14px;
}

.timeline-viewer {
  margin-top: 20px;
}

.association-panel {
  overflow: hidden;
}

.association-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.word-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
  margin-top: 24px;
}

.word-card {
  padding: 22px;
  border: 1px solid #e6d7c1;
  border-radius: 16px;
  background:
    radial-gradient(circle at 90% 10%, rgba(183, 65, 46, 0.06), transparent 24%),
    #fffdf8;
}

.word-head {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px 14px;
  margin-bottom: 16px;
}

.word-head h3 {
  margin: 0;
  color: #1f1a17;
  font-size: 26px;
}

.word-pinyin {
  color: #8a5b3d;
  font-size: 14px;
}

.word-section + .word-section {
  margin-top: 16px;
}

.card-section {
  overflow: hidden;
}

.culture-card-shell {
  margin-top: 24px;
}

.section-intro {
  max-width: 720px;
}

.error {
  color: #b23b2a;
}

@media (max-width: 900px) {
  .detail-hero,
  .detail-grid,
  .word-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .detail-page {
    padding: 22px 14px 48px;
  }

  .detail-hero,
  .detail-panel {
    padding: 22px 16px;
    border-radius: 16px;
  }

  .detail-grid,
  .detail-panel {
    margin-top: 18px;
  }

  .association-header {
    align-items: flex-start;
  }

  .association-button {
    width: 100%;
  }

  .hero-summary,
  .info-text,
  .status-text,
  .section-intro,
  .word-text {
    font-size: 15px;
  }

  .section-heading h2 {
    font-size: 21px;
  }

  .word-head h3 {
    font-size: 22px;
  }
}
</style>
