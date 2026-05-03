<script setup>
import { computed, ref, watch } from 'vue';

const props = defineProps({
  stage: {
    type: Object,
    default: null
  },
  kaishuCharacter: {
    type: String,
    default: ''
  },
  character: {
    type: String,
    default: ''
  }
});

const imageFailed = ref(false);

watch(
  () => props.stage?.key,
  () => {
    imageFailed.value = false;
  }
);

const displayCharacter = computed(() => props.kaishuCharacter || props.character || '字');
const shouldShowImage = computed(() => props.stage?.imageUrl && !imageFailed.value);

const handleImageError = () => {
  imageFailed.value = true;
};
</script>

<template>
  <section class="stage-viewer">
    <div class="stage-media">
      <img
        v-if="shouldShowImage"
        :src="stage.imageUrl"
        :alt="stage.name"
        class="stage-image"
        @error="handleImageError"
      />

      <div
        v-else-if="stage?.imageUrl && imageFailed"
        class="stage-placeholder"
      >
        <div class="placeholder-mark">古文字图片待补充</div>
        <p class="placeholder-text">当前阶段仍可查看文字说明。</p>
      </div>

      <div
        v-else
        class="stage-character"
      >
        {{ displayCharacter }}
      </div>
    </div>

    <div class="stage-content">
      <p class="stage-label">当前阶段</p>
      <h3 class="stage-title">{{ stage?.name }}</h3>
      <p class="stage-description">{{ stage?.description }}</p>
    </div>
  </section>
</template>

<style scoped>
.stage-viewer {
  display: grid;
  grid-template-columns: minmax(240px, 320px) minmax(0, 1fr);
  gap: 24px;
  align-items: stretch;
}

.stage-media,
.stage-content {
  min-height: 280px;
}

.stage-media {
  display: flex;
  align-items: stretch;
}

.stage-image,
.stage-placeholder,
.stage-character {
  width: 100%;
  border: 1px solid #e1d2bb;
  border-radius: 16px;
  background-color: #fffdf8;
}

.stage-image {
  object-fit: contain;
  padding: 20px;
}

.stage-placeholder,
.stage-character {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 28px;
}

.stage-character {
  color: #221a15;
  font-size: clamp(72px, 12vw, 124px);
  font-weight: 600;
  line-height: 1;
}

.placeholder-mark {
  color: #b7412e;
  font-size: 18px;
  font-weight: 600;
}

.placeholder-text {
  margin: 10px 0 0;
  color: #6c5d51;
  font-size: 15px;
  line-height: 1.7;
  text-align: center;
}

.stage-content {
  padding: 28px;
  border: 1px solid #e1d2bb;
  border-radius: 16px;
  background-color: #fffdf8;
}

.stage-label {
  margin: 0 0 12px;
  color: #b7412e;
  font-size: 14px;
  letter-spacing: 1px;
}

.stage-title {
  margin: 0;
  color: #231d18;
  font-size: 28px;
  font-weight: 600;
}

.stage-description {
  margin: 16px 0 0;
  color: #56493d;
  font-size: 16px;
  line-height: 1.9;
}

@media (max-width: 900px) {
  .stage-viewer {
    grid-template-columns: 1fr;
  }

  .stage-media,
  .stage-content {
    min-height: 0;
  }
}
</style>
