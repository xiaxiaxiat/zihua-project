import http from './http';

export const generateWordAssociation = (data) =>
  http.post('/ai/word-association', data, { timeout: 120000 });
