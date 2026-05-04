<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  createAdminCharacter,
  deleteAdminCharacter,
  getAdminCharacterByCode,
  updateAdminCharacter
} from '../api/adminCharacter';

const route = useRoute();
const router = useRouter();

const DEFAULT_STAGES = [
  { stageKey: 'jiaguwen', stageName: '甲骨文', sortOrder: 1 },
  { stageKey: 'jinwen', stageName: '金文', sortOrder: 2 },
  { stageKey: 'xiaozhuan', stageName: '小篆', sortOrder: 3 },
  { stageKey: 'lishu', stageName: '隶书', sortOrder: 4 },
  { stageKey: 'kaishu', stageName: '楷书', sortOrder: 5 }
];

const createStage = (stage) => ({
  stageKey: stage.stageKey,
  stageName: stage.stageName,
  imageUrl: '',
  description: '',
  sortOrder: stage.sortOrder
});

const createEmptyForm = () => ({
  code: '',
  character: '',
  pinyin: '',
  origin: '',
  meaning: '',
  culture: '',
  kaishuCharacter: '',
  storyTitle: '',
  storyBody: '',
  storyReviewed: false,
  cardSummary: '',
  sealText: '',
  note: '',
  stages: DEFAULT_STAGES.map(createStage)
});

const form = ref(createEmptyForm());
const loading = ref(false);
const saving = ref(false);
const deleting = ref(false);
const errorMessage = ref('');

const isEditMode = computed(() => Boolean(route.params.id));
const pageTitle = computed(() => (isEditMode.value ? '编辑汉字' : '新增汉字'));
const submitLabel = computed(() => (saving.value ? '保存中...' : '保存汉字'));

const readErrorMessage = (error, fallback) => error?.response?.data?.message || fallback;

const normalizeStages = (stages) => {
  if (!Array.isArray(stages) || stages.length === 0) {
    return DEFAULT_STAGES.map(createStage);
  }

  const mapped = DEFAULT_STAGES.map((defaultStage, index) => {
    const source = stages[index] || stages.find((item) => item.stageKey === defaultStage.stageKey) || defaultStage;
    return {
      stageKey: source.stageKey || defaultStage.stageKey,
      stageName: source.stageName || defaultStage.stageName,
      imageUrl: source.imageUrl || '',
      description: source.description || '',
      sortOrder: source.sortOrder ?? defaultStage.sortOrder
    };
  });

  return mapped.sort((left, right) => left.sortOrder - right.sortOrder);
};

const applyResponseToForm = (data) => {
  form.value = {
    code: data.code || '',
    character: data.character || '',
    pinyin: data.pinyin || '',
    origin: data.origin || '',
    meaning: data.meaning || '',
    culture: data.culture || '',
    kaishuCharacter: data.kaishuCharacter || '',
    storyTitle: data.storyTitle || '',
    storyBody: data.storyBody || '',
    storyReviewed: Boolean(data.storyReviewed),
    cardSummary: data.cardSummary || '',
    sealText: data.sealText || '',
    note: data.note || '',
    stages: normalizeStages(data.stages)
  };
};

const fetchCharacter = async () => {
  if (!isEditMode.value) {
    form.value = createEmptyForm();
    loading.value = false;
    errorMessage.value = '';
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    const response = await getAdminCharacterByCode(route.params.id);
    applyResponseToForm(response.data);
  } catch (error) {
    errorMessage.value = readErrorMessage(error, '编辑数据加载失败，请检查后端状态。');
  } finally {
    loading.value = false;
  }
};

const buildPayload = () => ({
  code: form.value.code,
  character: form.value.character,
  pinyin: form.value.pinyin,
  origin: form.value.origin,
  meaning: form.value.meaning,
  culture: form.value.culture,
  kaishuCharacter: form.value.kaishuCharacter,
  storyTitle: form.value.storyTitle,
  storyBody: form.value.storyBody,
  storyReviewed: form.value.storyReviewed,
  cardSummary: form.value.cardSummary,
  sealText: form.value.sealText,
  note: form.value.note,
  stages: form.value.stages.map((stage) => ({
    stageKey: stage.stageKey,
    stageName: stage.stageName,
    imageUrl: stage.imageUrl,
    description: stage.description,
    sortOrder: Number(stage.sortOrder)
  }))
});

const goBackToList = () => {
  router.push('/admin/characters');
};

const handleSubmit = async () => {
  saving.value = true;
  errorMessage.value = '';

  try {
    const payload = buildPayload();

    if (isEditMode.value) {
      await updateAdminCharacter(route.params.id, payload);
    } else {
      await createAdminCharacter(payload);
    }

    goBackToList();
  } catch (error) {
    errorMessage.value = readErrorMessage(error, '保存失败，请稍后重试。');
  } finally {
    saving.value = false;
  }
};

const handleDelete = async () => {
  if (!isEditMode.value) {
    return;
  }

  const confirmed = window.confirm(`确认删除汉字“${form.value.character || route.params.id}”吗？`);
  if (!confirmed) {
    return;
  }

  deleting.value = true;
  errorMessage.value = '';

  try {
    await deleteAdminCharacter(route.params.id);
    goBackToList();
  } catch (error) {
    errorMessage.value = readErrorMessage(error, '删除失败，请稍后重试。');
  } finally {
    deleting.value = false;
  }
};

onMounted(() => {
  fetchCharacter();
});

watch(
  () => route.params.id,
  () => {
    fetchCharacter();
  }
);
</script>

<template>
  <main class="admin-page">
    <section class="admin-shell">
      <div class="topbar">
        <router-link class="ghost-link" to="/admin/characters">返回管理列表</router-link>
        <router-link class="ghost-link" to="/">返回游客首页</router-link>
      </div>

      <section class="panel hero-panel">
        <p class="panel-mark">管理员端</p>
        <h1>{{ pageTitle }}</h1>
        <p class="panel-description">维护汉字主信息、字源故事和五阶段字形数据，保存后游客端将读取数据库最新内容。</p>
      </section>

      <section class="panel form-panel">
        <p v-if="loading" class="status-text">正在加载表单数据...</p>
        <p v-else-if="errorMessage" class="status-text error">{{ errorMessage }}</p>

        <form v-else class="form-grid" @submit.prevent="handleSubmit">
          <section class="form-section">
            <div class="section-heading">
              <span class="section-dot"></span>
              <h2>主信息</h2>
            </div>

            <div class="field-grid">
              <label class="field">
                <span>Code</span>
                <input v-model.trim="form.code" :readonly="isEditMode" type="text" placeholder="例如 ren-human" />
              </label>

              <label class="field">
                <span>汉字</span>
                <input v-model.trim="form.character" type="text" placeholder="例如 人" />
              </label>

              <label class="field">
                <span>拼音</span>
                <input v-model.trim="form.pinyin" type="text" placeholder="例如 ren" />
              </label>

              <label class="field">
                <span>造字法</span>
                <input v-model.trim="form.origin" type="text" placeholder="例如 象形字" />
              </label>

              <label class="field">
                <span>楷书字形</span>
                <input v-model.trim="form.kaishuCharacter" type="text" placeholder="例如 人" />
              </label>

              <label class="field checkbox-field">
                <span>故事审核</span>
                <div class="checkbox-line">
                  <input v-model="form.storyReviewed" type="checkbox" />
                  <span>已审核</span>
                </div>
              </label>
            </div>

            <label class="field full-width">
              <span>本义</span>
              <textarea v-model.trim="form.meaning" rows="3"></textarea>
            </label>

            <label class="field full-width">
              <span>文化寓意</span>
              <textarea v-model.trim="form.culture" rows="3"></textarea>
            </label>

            <label class="field full-width">
              <span>故事标题</span>
              <input v-model.trim="form.storyTitle" type="text" />
            </label>

            <label class="field full-width">
              <span>故事正文</span>
              <textarea v-model.trim="form.storyBody" rows="5"></textarea>
            </label>

            <label class="field full-width">
              <span>字卡摘要</span>
              <textarea v-model.trim="form.cardSummary" rows="3"></textarea>
            </label>

            <div class="field-grid">
              <label class="field">
                <span>印章文案</span>
                <input v-model.trim="form.sealText" type="text" />
              </label>
            </div>

            <label class="field full-width">
              <span>备注</span>
              <textarea v-model.trim="form.note" rows="3"></textarea>
            </label>
          </section>

          <section class="form-section">
            <div class="section-heading">
              <span class="section-dot"></span>
              <h2>五阶段字形</h2>
            </div>

            <div class="stage-list">
              <article v-for="(stage, index) in form.stages" :key="`${stage.stageKey}-${index}`" class="stage-card">
                <div class="stage-head">
                  <h3>{{ stage.stageName || `阶段 ${index + 1}` }}</h3>
                  <span class="stage-order">排序 {{ stage.sortOrder }}</span>
                </div>

                <div class="field-grid">
                  <label class="field">
                    <span>stageKey</span>
                    <input v-model.trim="stage.stageKey" type="text" />
                  </label>

                  <label class="field">
                    <span>stageName</span>
                    <input v-model.trim="stage.stageName" type="text" />
                  </label>

                  <label class="field">
                    <span>sortOrder</span>
                    <input v-model.number="stage.sortOrder" type="number" min="1" max="5" />
                  </label>
                </div>

                <label class="field full-width">
                  <span>imageUrl</span>
                  <input v-model.trim="stage.imageUrl" type="text" placeholder="/character-images/example/jgw.png" />
                </label>

                <label class="field full-width">
                  <span>description</span>
                  <textarea v-model.trim="stage.description" rows="3"></textarea>
                </label>
              </article>
            </div>
          </section>

          <div class="footer-actions">
            <button class="primary-button" type="submit" :disabled="saving">{{ submitLabel }}</button>
            <button class="secondary-button" type="button" @click="goBackToList">取消</button>
            <button
              v-if="isEditMode"
              class="danger-button"
              type="button"
              :disabled="deleting"
              @click="handleDelete"
            >
              {{ deleting ? '删除中...' : '删除汉字' }}
            </button>
          </div>
        </form>
      </section>
    </section>
  </main>
</template>

<style scoped>
.admin-page {
  min-height: 100vh;
  padding: 32px 24px 72px;
}

.admin-shell {
  width: min(100%, 1100px);
  margin: 0 auto;
}

.topbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 18px;
}

.ghost-link,
.primary-button,
.secondary-button,
.danger-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 42px;
  padding: 0 18px;
  border-radius: 999px;
  font: inherit;
  text-decoration: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.ghost-link,
.secondary-button {
  border: 1px solid #decfb5;
  background-color: rgba(255, 252, 246, 0.96);
  color: #2a221c;
}

.primary-button {
  border: 1px solid #b7412e;
  background-color: #b7412e;
  color: #fffaf4;
}

.danger-button {
  border: 1px solid rgba(178, 59, 42, 0.22);
  background-color: #fbf0ea;
  color: #b23b2a;
}

.ghost-link:hover,
.primary-button:hover,
.secondary-button:hover,
.danger-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(58, 44, 34, 0.08);
}

.panel {
  border: 1px solid #decfb5;
  border-radius: 18px;
  background-color: rgba(255, 252, 246, 0.95);
  box-shadow: 0 14px 30px rgba(58, 44, 34, 0.06);
}

.hero-panel,
.form-panel {
  padding: 28px;
}

.form-panel {
  margin-top: 22px;
}

.panel-mark,
.panel-description,
.status-text {
  margin: 0;
}

.panel-mark {
  color: #a53a2a;
  font-size: 14px;
  letter-spacing: 1px;
}

.hero-panel h1 {
  margin: 10px 0 0;
  color: #1f1a17;
  font-size: clamp(28px, 4vw, 40px);
}

.panel-description {
  margin-top: 14px;
  max-width: 760px;
  color: #5a4c41;
  font-size: 16px;
  line-height: 1.8;
}

.status-text {
  color: #4e4136;
  font-size: 16px;
}

.error {
  color: #b23b2a;
}

.form-grid {
  display: grid;
  gap: 24px;
}

.form-section {
  display: grid;
  gap: 18px;
}

.section-heading {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background-color: #b7412e;
}

.section-heading h2,
.stage-head h3 {
  margin: 0;
}

.section-heading h2 {
  color: #241d18;
  font-size: 24px;
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.field,
.full-width {
  display: grid;
  gap: 10px;
}

.full-width {
  grid-column: 1 / -1;
}

.field span {
  color: #7b6758;
  font-size: 14px;
}

.field input,
.field textarea {
  width: 100%;
  border: 1px solid #decfb5;
  border-radius: 14px;
  background-color: #fffdf8;
  color: #2f2720;
  font: inherit;
}

.field input {
  min-height: 48px;
  padding: 0 16px;
}

.field textarea {
  padding: 14px 16px;
  resize: vertical;
}

.field input:focus,
.field textarea:focus {
  outline: none;
  border-color: #c4563f;
  box-shadow: 0 0 0 3px rgba(196, 86, 63, 0.12);
}

.field input[readonly] {
  background-color: #f8f0e3;
  color: #78695d;
}

.checkbox-field {
  align-content: start;
}

.checkbox-line {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 48px;
  padding: 0 16px;
  border: 1px solid #decfb5;
  border-radius: 14px;
  background-color: #fffdf8;
  color: #2f2720;
}

.stage-list {
  display: grid;
  gap: 18px;
}

.stage-card {
  padding: 20px;
  border: 1px solid #e6d7c1;
  border-radius: 16px;
  background:
    radial-gradient(circle at 92% 10%, rgba(183, 65, 46, 0.06), transparent 24%),
    #fffdf8;
}

.stage-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.stage-head h3 {
  color: #241d18;
  font-size: 20px;
}

.stage-order {
  color: #8b7361;
  font-size: 13px;
}

.footer-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: flex-start;
}

.primary-button:disabled,
.secondary-button:disabled,
.danger-button:disabled {
  cursor: wait;
  opacity: 0.75;
}

@media (max-width: 800px) {
  .field-grid {
    grid-template-columns: 1fr;
  }

  .stage-head {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 640px) {
  .admin-page {
    padding: 22px 14px 48px;
  }

  .hero-panel,
  .form-panel {
    padding: 22px 16px;
    border-radius: 16px;
  }
}
</style>
