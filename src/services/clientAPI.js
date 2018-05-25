import axios from 'axios';
import * as api from './api';

export const HTTP_CODE = {
  RES_OK: 0,
  RES_TOKEN_FAIL: 401,
  RES_PARAM_WRONG: 201,
  RES_VALIDATE_ERROR: 800,
  RES_REGISTER_FAIL: 801,
  RES_EXCEPTION_FAIL: 999,
};

export const clientAPI = axios.create({
  baseURL: api.BASE_URL,
});

export const configHeaderToken = token => {
  clientAPI.defaults.headers.common.AccessToken = token;
};
