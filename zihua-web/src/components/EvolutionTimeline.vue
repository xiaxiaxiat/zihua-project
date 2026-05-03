<script setup>
defineProps({
  stages: {
    type: Array,
    default: () => []
  },
  activeStageKey: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['select']);

const selectStage = (stageKey) => {
  emit('select', stageKey);
};
</script>

<template>
  <div class="timeline">
    <button
      v-for="stage in stages"
      :key="stage.key"
      type="button"
      class="timeline-item"
      :class="{ active: stage.key === activeStageKey }"
      @click="selectStage(stage.key)"
    >
      <span class="timeline-name">{{ stage.name }}</span>
    </button>
  </div>
</template>

<style scoped>
.timeline {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.timeline-item {
  min-width: 108px;
  padding: 12px 16px;
  border: 1px solid #ddccb0;
  border-radius: 12px;
  background-color: #fffdf8;
  color: #58493f;
  font: inherit;
  cursor: pointer;
  transition: border-color 0.2s ease, background-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.timeline-item:hover {
  transform: translateY(-1px);
  border-color: #c99b6c;
}

.timeline-item.active {
  border-color: #b7412e;
  background-color: #fbf1e8;
  color: #231d18;
}

.timeline-name {
  display: block;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
}

@media (max-width: 640px) {
  .timeline-item {
    flex: 1 1 calc(50% - 6px);
    min-width: 0;
  }
}
</style>
