import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import CharacterDetailView from '../views/CharacterDetailView.vue';
import AdminCharacterListView from '../views/AdminCharacterListView.vue';
import AdminCharacterFormView from '../views/AdminCharacterFormView.vue';
import AdminLoginView from '../views/AdminLoginView.vue';

const isAdminLoggedIn = () => localStorage.getItem('adminLoggedIn') === 'true';

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/character/:id',
    name: 'character-detail',
    component: CharacterDetailView
  },
  {
    path: '/admin/login',
    name: 'admin-login',
    component: AdminLoginView
  },
  {
    path: '/admin/characters',
    name: 'admin-character-list',
    component: AdminCharacterListView,
    meta: {
      requiresAdminAuth: true
    }
  },
  {
    path: '/admin/characters/new',
    name: 'admin-character-new',
    component: AdminCharacterFormView,
    meta: {
      requiresAdminAuth: true
    }
  },
  {
    path: '/admin/characters/:id/edit',
    name: 'admin-character-edit',
    component: AdminCharacterFormView,
    meta: {
      requiresAdminAuth: true
    }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  if (to.path === '/admin/login') {
    if (isAdminLoggedIn()) {
      return '/admin/characters';
    }
    return true;
  }

  if (to.meta.requiresAdminAuth && !isAdminLoggedIn()) {
    return '/admin/login';
  }

  return true;
});

export default router;
