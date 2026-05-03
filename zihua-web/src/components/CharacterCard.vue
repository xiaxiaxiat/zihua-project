<script setup>
defineProps({
  item: {
    type: Object,
    required: true
  }
});
</script>

<template>
  <router-link
    class="character-card"
    :to="`/character/${item.id}`"
  >
    <div class="card-character">{{ item.character }}</div>
    <div class="card-meta">
      <div class="card-topline">
        <p class="card-pinyin">{{ item.pinyin }}</p>
        <p class="card-origin">{{ item.origin }}</p>
      </div>
      <p class="card-culture">{{ item.culture }}</p>
      <p class="card-summary">{{ item.cardMeta.summary }}</p>
      <p class="card-action">查看字源演变</p>
    </div>
  </router-link>
</template>

<style scoped>
.character-card {
  display: flex;
  gap: 18px;
  align-items: stretch;
  min-height: 230px;
  padding: 20px;
  border: 1px solid #e1d2bb;
  border-radius: 16px;
  background-color: #fffdf8;
  color: inherit;
  text-decoration: none;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.character-card:hover {
  transform: translateY(-3px);
  border-color: #d4b791;
  box-shadow: 0 12px 24px rgba(58, 44, 34, 0.08);
}

.character-card:focus-visible {
  outline: 3px solid rgba(183, 65, 46, 0.18);
  outline-offset: 3px;
}

.card-character {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 84px;
  min-width: 84px;
  height: 84px;
  border-radius: 12px;
  background:
    radial-gradient(circle at 72% 18%, rgba(183, 65, 46, 0.12), transparent 30%),
    #f3e6d4;
  color: #221a15;
  font-size: 42px;
  font-weight: 600;
}

.card-meta {
  display: flex;
  flex: 1;
  flex-direction: column;
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
.card-summary,
.card-action {
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

.card-action {
  margin-top: auto;
  padding-top: 16px;
  color: #b7412e;
  font-size: 14px;
  font-weight: 600;
}

@media (max-width: 640px) {
  .character-card {
    min-height: 0;
    align-items: flex-start;
    gap: 14px;
    padding: 18px 16px;
  }

  .card-character {
    width: 72px;
    min-width: 72px;
    height: 72px;
    font-size: 36px;
  }
}
</style>
