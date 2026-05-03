<script setup>
defineProps({
  item: {
    type: Object,
    required: true
  }
});
</script>

<template>
  <article class="character-card">
    <div class="card-character">{{ item.character }}</div>
    <div class="card-meta">
      <div class="card-topline">
        <p class="card-pinyin">{{ item.pinyin }}</p>
        <p class="card-origin">{{ item.origin }}</p>
      </div>
      <p class="card-culture">{{ item.culture }}</p>
      <p class="card-summary">{{ item.cardMeta.summary }}</p>
    </div>
  </article>
</template>

<style scoped>
.character-card {
  display: flex;
  gap: 18px;
  align-items: center;
  min-height: 220px;
  padding: 22px 20px;
  border: 1px solid #e1d2bb;
  border-radius: 14px;
  background-color: #fffdf8;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.character-card:hover {
  transform: translateY(-2px);
  border-color: #d4b791;
  box-shadow: 0 12px 24px rgba(58, 44, 34, 0.08);
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

.card-topline {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: baseline;
  margin-bottom: 10px;
}

.card-pinyin,
.card-origin,
.card-culture,
.card-summary {
  margin: 0;
}

.card-pinyin {
  color: #b7412e;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.card-origin {
  color: #7b6758;
  font-size: 14px;
}

.card-culture {
  color: #53463c;
  font-size: 15px;
  line-height: 1.8;
}

.card-summary {
  margin-top: 12px;
  color: #2f2720;
  font-size: 15px;
  line-height: 1.7;
}

@media (max-width: 640px) {
  .character-card {
    min-height: 0;
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
