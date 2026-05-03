<script setup>
import { computed } from 'vue';

const props = defineProps({
  detail: {
    type: Object,
    required: true
  },
  cardElementId: {
    type: String,
    required: true
  }
});

const createExcerpt = (text, maxLength) => {
  const normalizedText = String(text || '').replace(/\s+/g, ' ').trim();

  if (!normalizedText) {
    return '内容整理中，敬请期待。';
  }

  if (normalizedText.length <= maxLength) {
    return normalizedText;
  }

  return `${normalizedText.slice(0, maxLength).trim()}……`;
};

const meaningExcerpt = computed(() => createExcerpt(props.detail?.meaning, 42));
const storyExcerpt = computed(() => createExcerpt(props.detail?.story?.body, 86));
const summaryText = computed(() => props.detail?.cardMeta?.summary || '一字一源，一笔一意。');
const sealText = computed(() => props.detail?.cardMeta?.sealText || '字活');
</script>

<template>
  <article :id="cardElementId" class="culture-card">
    <header class="card-header">
      <div>
        <p class="card-brand">字活</p>
        <p class="card-tagline">汉字字源与文化字卡</p>
      </div>
      <div class="seal-mark" :aria-label="sealText">{{ sealText }}</div>
    </header>

    <section class="card-hero">
      <h3 class="hero-character">{{ detail.character }}</h3>
      <div class="hero-meta">
        <span class="meta-chip">{{ detail.pinyin }}</span>
        <span class="meta-chip">{{ detail.origin }}</span>
      </div>
    </section>

    <section class="summary-panel">
      <p class="summary-label">字卡提要</p>
      <p class="summary-text">{{ summaryText }}</p>
    </section>

    <section class="card-content">
      <article class="info-block">
        <p class="info-label">本义</p>
        <p class="info-text">{{ meaningExcerpt }}</p>
      </article>

      <article class="info-block">
        <p class="info-label">文化寓意</p>
        <p class="info-text">{{ detail.culture }}</p>
      </article>

      <article class="info-block info-block-wide">
        <p class="info-label">字源故事摘记</p>
        <p class="info-text">{{ storyExcerpt }}</p>
      </article>
    </section>

    <footer class="card-footer">
      <p class="footer-note">AI 汉字字源演变与互动创作平台</p>
    </footer>
  </article>
</template>

<style scoped>
.culture-card {
  width: min(100%, 720px);
  max-width: 100%;
  margin: 0 auto;
  padding: 28px;
  border: 1px solid #d8c7ab;
  border-radius: 18px;
  background:
    radial-gradient(circle at top right, rgba(183, 65, 46, 0.08), transparent 26%),
    linear-gradient(180deg, #fffdf7 0%, #f6efe3 100%);
  box-shadow: 0 18px 36px rgba(58, 44, 34, 0.08);
  overflow-wrap: anywhere;
}

.card-header,
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.card-brand,
.card-tagline,
.summary-label,
.summary-text,
.info-label,
.info-text,
.footer-note {
  margin: 0;
}

.card-brand {
  color: #2a221c;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 1px;
}

.card-tagline {
  margin-top: 6px;
  color: #8b7665;
  font-size: 13px;
}

.seal-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 78px;
  min-width: 78px;
  height: 78px;
  border: 2px solid #b7412e;
  border-radius: 18px;
  background-color: rgba(183, 65, 46, 0.08);
  color: #b7412e;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 4px;
  writing-mode: vertical-rl;
  text-orientation: mixed;
}

.card-hero {
  margin-top: 28px;
  text-align: center;
}

.hero-character {
  margin: 0;
  color: #1f1a17;
  font-size: clamp(88px, 16vw, 156px);
  line-height: 1;
  font-weight: 600;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 18px;
}

.meta-chip {
  padding: 8px 16px;
  border: 1px solid #decfb5;
  border-radius: 999px;
  background-color: rgba(255, 252, 246, 0.92);
  color: #5e4e42;
  font-size: 14px;
}

.summary-panel {
  margin-top: 24px;
  padding: 18px 20px;
  border: 1px solid #e0d1ba;
  border-radius: 14px;
  background-color: rgba(255, 253, 248, 0.94);
}

.summary-label {
  color: #b7412e;
  font-size: 13px;
  letter-spacing: 1px;
}

.summary-text {
  margin-top: 10px;
  color: #2f2720;
  font-size: 20px;
  line-height: 1.8;
}

.card-content {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.info-block {
  padding: 18px;
  border: 1px solid #e0d1ba;
  border-radius: 14px;
  background-color: rgba(255, 253, 248, 0.9);
}

.info-block-wide {
  grid-column: 1 / -1;
}

.info-label {
  color: #b7412e;
  font-size: 13px;
  letter-spacing: 1px;
}

.info-text {
  margin-top: 10px;
  color: #4e4136;
  font-size: 15px;
  line-height: 1.9;
}

.card-footer {
  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px solid rgba(183, 65, 46, 0.12);
}

.footer-note {
  color: #8a7767;
  font-size: 12px;
  letter-spacing: 0.5px;
}

@media (max-width: 640px) {
  .culture-card {
    padding: 20px 14px;
    border-radius: 16px;
  }

  .card-header,
  .card-footer {
    align-items: flex-start;
  }

  .seal-mark {
    width: 64px;
    min-width: 64px;
    height: 64px;
    border-radius: 14px;
    font-size: 20px;
    letter-spacing: 2px;
  }

  .card-content {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .hero-character {
    font-size: clamp(74px, 28vw, 120px);
  }

  .summary-panel,
  .info-block {
    padding: 16px 14px;
  }

  .summary-text {
    font-size: 17px;
  }

  .footer-note {
    line-height: 1.6;
  }
}
</style>
