import http from './http';

export const getCharacters = () => http.get('/characters');

export const getCharacterById = (id) => http.get(`/characters/${id}`);
