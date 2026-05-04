import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import CharacterDetailView from '../views/CharacterDetailView.vue';
import AdminCharacterListView from '../views/AdminCharacterListView.vue';
import AdminCharacterFormView from '../views/AdminCharacterFormView.vue';

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
    path: '/admin/characters',
    name: 'admin-character-list',
    component: AdminCharacterListView
  },
  {
    path: '/admin/characters/new',
    name: 'admin-character-new',
    component: AdminCharacterFormView
  },
  {
    path: '/admin/characters/:id/edit',
    name: 'admin-character-edit',
    component: AdminCharacterFormView
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
