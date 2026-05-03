<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import HanziWriter from 'hanzi-writer';

const props = defineProps({
  character: {
    type: String,
    required: true
  }
});

const writerContainer = ref(null);
const writer = ref(null);
const writerReady = ref(false);
const hintMessage = ref('请按照笔顺描摹这个字。');
const quizActive = ref(false);

const writerOptions = {
  width: 260,
  height: 260,
  padding: 18,
  showOutline: true,
  showCharacter: false,
  strokeAnimationSpeed: 1,
  delayBetweenStrokes: 180,
  outlineColor: '#d8c8ae',
  strokeColor: '#221a15',
  radicalColor: '#8a5b3d',
  highlightColor: '#b7412e',
  drawingColor: '#2f2720'
};

const clearWriterContainer = () => {
  if (writerContainer.value) {
    writerContainer.value.innerHTML = '';
  }
};

const mountWriter = async () => {
  writerReady.value = false;
  quizActive.value = false;
  hintMessage.value = '请按照笔顺描摹这个字。';

  if (!writerContainer.value || !props.character) {
    return;
  }

  clearWriterContainer();
  writer.value = null;

  await nextTick();

  writer.value = HanziWriter.create(writerContainer.value, props.character, writerOptions);
  writerReady.value = true;
};

const resetWriter = async (message = '请按照笔顺描摹这个字。') => {
  if (writer.value) {
    writer.value.cancelQuiz();
  }

  hintMessage.value = message;
  await mountWriter();
};

const playStrokeOrder = async () => {
  await resetWriter('正在播放楷书笔顺。');

  if (writer.value) {
    writer.value.animateCharacter({
      onComplete: () => {
        hintMessage.value = '笔顺播放完成，可重新播放或开始描摹。';
      }
    });
  }
};

const replayStrokeOrder = async () => {
  await resetWriter('已重新开始播放笔顺。');

  if (writer.value) {
    writer.value.animateCharacter({
      onComplete: () => {
        hintMessage.value = '笔顺已重新播放完成。';
      }
    });
  }
};

const startQuiz = async () => {
  await resetWriter('请按照笔顺描摹这个字。');

  if (writer.value) {
    quizActive.value = true;
    writer.value.quiz({
      onComplete: () => {
        quizActive.value = false;
        hintMessage.value = '描摹完成后可继续查看字源故事。';
      }
    });
  }
};

const clearAndReset = async () => {
  await resetWriter('已清空当前描摹，可重新播放或再次练习。');
};

onMounted(() => {
  mountWriter();
});

watch(
  () => props.character,
  () => {
    mountWriter();
  }
);

onBeforeUnmount(() => {
  if (writer.value) {
    writer.value.cancelQuiz();
  }
  clearWriterContainer();
  writer.value = null;
});
</script>

<template>
  <article class="stroke-panel">
    <div class="section-heading">
      <span class="section-dot"></span>
      <h2>楷书笔顺与描摹</h2>
    </div>

    <div class="player-layout">
      <div class="writer-board">
        <div ref="writerContainer" class="writer-canvas"></div>
      </div>

      <div class="player-controls">
        <p class="player-hint">{{ hintMessage }}</p>
        <div class="button-group">
          <button type="button" class="action-button" :disabled="!writerReady" @click="playStrokeOrder">
            播放笔顺
          </button>
          <button type="button" class="action-button" :disabled="!writerReady" @click="replayStrokeOrder">
            重新播放
          </button>
          <button type="button" class="action-button accent" :disabled="!writerReady" @click="startQuiz">
            开始描摹
          </button>
          <button type="button" class="action-button" :disabled="!writerReady" @click="clearAndReset">
            清空重置
          </button>
        </div>
        <p class="player-note">
          {{ quizActive ? '描摹中：请按正确笔顺完成当前汉字。' : '描摹完成后可继续查看字源故事。' }}
        </p>
      </div>
    </div>
  </article>
</template>

<style scoped>
.stroke-panel {
  margin-top: 26px;
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

.section-heading h2 {
  margin: 0;
  color: #241d18;
  font-size: 24px;
  font-weight: 600;
}

.player-layout {
  display: grid;
  grid-template-columns: minmax(220px, 280px) minmax(0, 1fr);
  gap: 24px;
  align-items: start;
  min-width: 0;
}

.writer-board {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 280px;
  padding: 16px;
  border: 1px solid #e1d2bb;
  border-radius: 18px;
  background-color: #fffdf8;
  min-width: 0;
}

.writer-canvas {
  width: 260px;
  max-width: 100%;
  aspect-ratio: 1 / 1;
}

.player-controls {
  min-width: 0;
  padding: 24px;
  border: 1px solid #e1d2bb;
  border-radius: 18px;
  background-color: #fffdf8;
}

.player-hint,
.player-note {
  margin: 0;
  color: #4e4136;
  font-size: 16px;
  line-height: 1.8;
}

.button-group {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.action-button {
  min-height: 44px;
  padding: 0 14px;
  border: 1px solid #d9c8ab;
  border-radius: 14px;
  background-color: #fdf9f2;
  color: #2a221c;
  font: inherit;
  cursor: pointer;
  transition: border-color 0.2s ease, background-color 0.2s ease, transform 0.2s ease;
}

.action-button:hover:not(:disabled) {
  transform: translateY(-1px);
  border-color: #c99b6c;
}

.action-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.action-button.accent {
  border-color: #c4563f;
  background-color: #fbf0ea;
}

.player-note {
  margin-top: 18px;
  color: #7b6758;
  font-size: 14px;
}

@media (max-width: 900px) {
  .player-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .stroke-panel {
    margin-top: 18px;
    padding: 22px 16px;
    border-radius: 16px;
  }

  .writer-board {
    min-height: 0;
    padding: 14px;
    border-radius: 16px;
  }

  .writer-canvas {
    width: min(220px, 100%);
  }

  .player-controls {
    padding: 18px 14px;
    border-radius: 16px;
  }

  .button-group {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .action-button {
    width: 100%;
  }
}
</style>
