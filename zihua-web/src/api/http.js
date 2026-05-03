import axios from 'axios';

const http = axios.create({
  baseURL: '/api',
  timeout: 5000
});

export default http;
