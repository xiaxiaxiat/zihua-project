<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { deleteAdminCharacter, getAdminCharacters } from '../api/adminCharacter';

const router = useRouter();

const items = ref([]);
const loading = ref(true);
const errorMessage = ref('');
const deletingCode = ref('');

const hasData = computed(() => items.value.length > 0);

const formatUpdatedAt = (value) => {
  if (!value) {
    return '未记录';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const readErrorMessage = (error, fallback) => {
  return error?.response?.data?.message || fallback;
};

const fetchCharacters = async () => {
  loading.value = true;
  errorMessage.value = '';

  try {
    const response = await getAdminCharacters();
    items.value = response.data;
  } catch (error) {
    items.value = [];
    errorMessage.value = readErrorMessage(error, '管理员列表加载失败，请检查后端状态。');
  } finally {
    loading.value = false;
  }
};

const goToCreate = () => {
  router.push('/admin/characters/new');
};

const goToEdit = (code) => {
  router.push(`/admin/characters/${code}/edit`);
};

const handleLogout = () => {
  localStorage.removeItem('adminLoggedIn');
  router.push('/admin/login');
};

const handleDelete = async (code, character) => {
  const confirmed = window.confirm(`确认删除汉字“${character}” (${code}) 吗？`);
  if (!confirmed) {
    return;
  }

  deletingCode.value = code;
  errorMessage.value = '';

  try {
    await deleteAdminCharacter(code);
    await fetchCharacters();
  } catch (error) {
    errorMessage.value = readErrorMessage(error, '删除失败，请稍后重试。');
  } finally {
    deletingCode.value = '';
  }
};

onMounted(() => {
  fetchCharacters();
});
</script>

<template>
  <main class="admin-page">
    <section class="admin-shell">
      <header class="hero">
        <div>
          <p class="hero-mark">管理员端</p>
          <h1 class="hero-title">汉字数据管理</h1>
          <p class="hero-description">在这里新增、编辑和删除字库数据，不影响游客端访问路径。</p>
        </div>
        <div class="hero-actions">
          <button class="primary-button" type="button" @click="goToCreate">新增汉字</button>
          <button class="secondary-button" type="button" @click="handleLogout">退出登录</button>
          <router-link class="secondary-button" to="/">返回游客首页</router-link>
        </div>
      </header>

      <section class="panel">
        <div class="panel-heading">
          <div>
            <p class="panel-mark">管理列表</p>
            <h2>全部汉字</h2>
          </div>
          <p class="panel-count">共 {{ items.length }} 条</p>
        </div>

        <p v-if="loading" class="status-text">正在加载管理员列表...</p>
        <p v-else-if="errorMessage" class="status-text error">{{ errorMessage }}</p>
        <div v-else-if="!hasData" class="empty-state">
          <p class="empty-title">当前没有可管理的汉字</p>
          <button class="primary-button" type="button" @click="goToCreate">立即新增</button>
        </div>
        <div v-else class="table-wrap">
          <table class="admin-table">
            <thead>
              <tr>
                <th>Code</th>
                <th>汉字</th>
                <th>拼音</th>
                <th>造字法</th>
                <th>是否审核</th>
                <th>更新时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in items" :key="item.id">
                <td class="mono">{{ item.id }}</td>
                <td class="character-cell">{{ item.character }}</td>
                <td>{{ item.pinyin || '-' }}</td>
                <td>{{ item.origin || '-' }}</td>
                <td>
                  <span class="review-badge" :class="{ reviewed: item.storyReviewed }">
                    {{ item.storyReviewed ? '已审核' : '待审核' }}
                  </span>
                </td>
                <td>{{ formatUpdatedAt(item.updatedAt) }}</td>
                <td>
                  <div class="action-group">
                    <button class="inline-button" type="button" @click="goToEdit(item.id)">编辑</button>
                    <button
                      class="inline-button danger"
                      type="button"
                      :disabled="deletingCode === item.id"
                      @click="handleDelete(item.id, item.character)"
                    >
                      {{ deletingCode === item.id ? '删除中...' : '删除' }}
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </section>
  </main>
</template>

<style scoped>
.admin-page {
  min-height: 100vh;
  padding: 36px 24px 72px;
}

.admin-shell {
  width: min(100%, 1180px);
  margin: 0 auto;
}

.hero,
.panel {
  border: 1px solid #decfb5;
  border-radius: 18px;
  background-color: rgba(255, 252, 246, 0.95);
  box-shadow: 0 14px 30px rgba(58, 44, 34, 0.06);
}

.hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 30px 32px;
  background:
    radial-gradient(circle at 92% 10%, rgba(183, 65, 46, 0.08), transparent 24%),
    rgba(255, 252, 246, 0.95);
}

.hero-mark,
.hero-title,
.hero-description,
.panel-mark,
.panel-count,
.status-text,
.empty-title {
  margin: 0;
}

.hero-mark,
.panel-mark {
  color: #a53a2a;
  font-size: 14px;
  letter-spacing: 1px;
}

.hero-title {
  margin-top: 8px;
  color: #1f1a17;
  font-size: clamp(30px, 4vw, 42px);
}

.hero-description {
  margin-top: 14px;
  max-width: 620px;
  color: #5a4c41;
  font-size: 16px;
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: flex-end;
  gap: 12px;
}

.primary-button,
.secondary-button,
.inline-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 42px;
  padding: 0 18px;
  border-radius: 999px;
  font: inherit;
  text-decoration: none;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.primary-button {
  border: 1px solid #b7412e;
  background-color: #b7412e;
  color: #fffaf4;
}

.secondary-button,
.inline-button {
  border: 1px solid #decfb5;
  background-color: #fffdf8;
  color: #2a221c;
}

.primary-button:hover,
.secondary-button:hover,
.inline-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(58, 44, 34, 0.08);
}

.panel {
  margin-top: 24px;
  padding: 28px;
}

.panel-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 20px;
}

.panel-heading h2 {
  margin: 8px 0 0;
  color: #241d18;
  font-size: 24px;
}

.panel-count {
  color: #6a5a4d;
  font-size: 14px;
}

.status-text {
  color: #4e4136;
  font-size: 16px;
  line-height: 1.8;
}

.error {
  color: #b23b2a;
}

.empty-state {
  display: grid;
  gap: 14px;
  justify-items: center;
  padding: 36px 18px;
  border: 1px dashed #dcc9ab;
  border-radius: 16px;
  background:
    linear-gradient(180deg, rgba(255, 253, 248, 0.96), rgba(249, 241, 230, 0.88));
}

.empty-title {
  color: #5a4c41;
  font-size: 16px;
}

.table-wrap {
  overflow-x: auto;
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 860px;
}

.admin-table th,
.admin-table td {
  padding: 14px 12px;
  border-bottom: 1px solid #eadcc7;
  text-align: left;
  color: #352c25;
  font-size: 14px;
}

.admin-table th {
  color: #7b6758;
  font-weight: 600;
}

.mono {
  font-family: "Consolas", "Courier New", monospace;
}

.character-cell {
  font-size: 24px;
  color: #221a15;
}

.review-badge {
  display: inline-flex;
  min-height: 30px;
  align-items: center;
  padding: 0 12px;
  border-radius: 999px;
  background-color: #fbf0ea;
  color: #a04835;
}

.reviewed {
  background-color: #edf6ee;
  color: #2f7a49;
}

.action-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.danger {
  border-color: rgba(178, 59, 42, 0.22);
  color: #b23b2a;
}

.inline-button:disabled {
  cursor: wait;
  opacity: 0.7;
}

@media (max-width: 900px) {
  .hero,
  .panel-heading {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-actions {
    justify-content: flex-start;
  }
}

@media (max-width: 640px) {
  .admin-page {
    padding: 24px 14px 48px;
  }

  .hero,
  .panel {
    padding: 22px 16px;
    border-radius: 16px;
  }
}
</style>
