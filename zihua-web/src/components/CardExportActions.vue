<script setup>
import { onBeforeUnmount, ref } from 'vue';
import html2canvas from 'html2canvas';

const props = defineProps({
  cardElementId: {
    type: String,
    required: true
  },
  character: {
    type: String,
    default: ''
  }
});

const exporting = ref(false);
const statusMessage = ref('');
const statusType = ref('idle');

let successTimerId = null;

const clearSuccessTimer = () => {
  if (successTimerId) {
    window.clearTimeout(successTimerId);
    successTimerId = null;
  }
};

const setStatus = (message, type = 'idle', autoClear = false) => {
  clearSuccessTimer();
  statusMessage.value = message;
  statusType.value = type;

  if (autoClear) {
    successTimerId = window.setTimeout(() => {
      statusMessage.value = '';
      statusType.value = 'idle';
      successTimerId = null;
    }, 3000);
  }
};

const createFileName = () => {
  const normalizedCharacter = String(props.character || '').trim();
  return normalizedCharacter ? `zihua-${normalizedCharacter}.png` : 'zihua-card.png';
};

const downloadBlob = (blob) => {
  const downloadLink = document.createElement('a');
  const objectUrl = URL.createObjectURL(blob);

  downloadLink.href = objectUrl;
  downloadLink.download = createFileName();
  document.body.appendChild(downloadLink);
  downloadLink.click();
  document.body.removeChild(downloadLink);
  URL.revokeObjectURL(objectUrl);
};

const exportCard = async () => {
  const targetElement = document.getElementById(props.cardElementId);

  if (!targetElement) {
    setStatus('导出失败，请稍后重试', 'error');
    return;
  }

  exporting.value = true;
  setStatus('正在生成字卡……');

  try {
    const canvas = await html2canvas(targetElement, {
      backgroundColor: '#f7f1e6',
      scale: Math.max(2, Math.min(window.devicePixelRatio || 1, 3)),
      useCORS: true,
      logging: false
    });

    const blob = await new Promise((resolve) => {
      canvas.toBlob(resolve, 'image/png');
    });

    if (!blob) {
      throw new Error('PNG blob generation failed.');
    }

    downloadBlob(blob);
    setStatus('字卡已生成', 'success', true);
  } catch (error) {
    setStatus('导出失败，请稍后重试', 'error');
  } finally {
    exporting.value = false;
  }
};

onBeforeUnmount(() => {
  clearSuccessTimer();
});
</script>

<template>
  <div class="export-actions">
    <button type="button" class="export-button" :disabled="exporting" @click="exportCard">
      {{ exporting ? '正在生成字卡……' : '导出字卡 PNG' }}
    </button>
    <p v-if="statusMessage" class="status-text" :class="statusType">{{ statusMessage }}</p>
  </div>
</template>

<style scoped>
.export-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-top: 20px;
}

.export-button {
  min-height: 46px;
  padding: 0 24px;
  border: 1px solid #c4563f;
  border-radius: 999px;
  background-color: #fbf0ea;
  color: #2a221c;
  font: inherit;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.export-button:hover:not(:disabled) {
  transform: translateY(-1px);
  border-color: #b7412e;
  box-shadow: 0 10px 24px rgba(183, 65, 46, 0.14);
}

.export-button:disabled {
  opacity: 0.72;
  cursor: not-allowed;
}

.status-text {
  margin: 0;
  color: #5d4f43;
  font-size: 14px;
  line-height: 1.7;
  text-align: center;
}

.status-text.success {
  color: #7b3d2e;
}

.status-text.error {
  color: #b23b2a;
}

@media (max-width: 640px) {
  .export-actions {
    align-items: stretch;
  }

  .export-button {
    width: 100%;
  }
}
</style>
