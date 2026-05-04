import http from './http';

export const getAdminCharacters = () => http.get('/admin/characters');

export const getAdminCharacterByCode = (code) => http.get(`/admin/characters/${code}`);

export const createAdminCharacter = (data) => http.post('/admin/characters', data);

export const updateAdminCharacter = (code, data) => http.put(`/admin/characters/${code}`, data);

export const deleteAdminCharacter = (code) => http.delete(`/admin/characters/${code}`);
