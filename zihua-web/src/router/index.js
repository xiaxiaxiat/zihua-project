import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import CharacterDetailView from '../views/CharacterDetailView.vue';

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
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
