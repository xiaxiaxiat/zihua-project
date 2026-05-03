<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { getCharacterById } from '../api/character';
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

const fetchDetail = async () => {
  detailLoading.value = true;
  detailError.value = false;
  detail.value = null;
  activeStageKey.value = '';

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
      <router-link to="/" class="back-link">返回首页</router-link>

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
          <article class="detail-panel">
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

          <article class="detail-panel">
            <div class="section-heading">
              <span class="section-dot"></span>
              <h2>{{ detail.story.title }}</h2>
            </div>
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
  padding: 40px 24px 72px;
}

.detail-shell {
  width: min(100%, 1080px);
  margin: 0 auto;
}

.back-link,
.back-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 42px;
  padding: 0 16px;
  border: 1px solid #decfb5;
  border-radius: 999px;
  background-color: rgba(255, 252, 246, 0.94);
  color: #2a221c;
  text-decoration: none;
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
  border-radius: 16px;
  background-color: rgba(255, 252, 246, 0.94);
  box-shadow: 0 14px 30px rgba(58, 44, 34, 0.06);
}

.detail-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(260px, 0.8fr);
  gap: 24px;
  padding: 32px;
}

.hero-mark,
.hero-summary,
.info-label,
.info-text,
.status-text,
.section-intro {
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
  margin-top: 24px;
}

.detail-panel {
  margin-top: 24px;
  padding: 28px;
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

.info-label {
  margin-bottom: 10px;
  color: #b7412e;
  font-size: 14px;
  letter-spacing: 1px;
}

.info-text,
.status-text,
.section-intro {
  color: #4e4136;
  font-size: 16px;
  line-height: 1.9;
}

.timeline-viewer {
  margin-top: 20px;
}

.card-section {
  overflow: hidden;
}

.culture-card-shell {
  margin-top: 22px;
}

.section-intro {
  max-width: 640px;
}

.error {
  color: #b23b2a;
}

@media (max-width: 900px) {
  .detail-hero,
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .detail-page {
    padding: 24px 16px 48px;
  }

  .detail-hero,
  .detail-panel {
    padding: 22px 18px;
  }
}
</style>
