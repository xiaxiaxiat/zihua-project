import http from './http';

export const generateCharacterDraft = (data) =>
  http.post('/admin/ai/character-draft', data, { timeout: 120000 });
