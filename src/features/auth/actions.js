// @flow
import { NavigationActions } from 'react-navigation';
// import { Dispatch } from 'redux';
import _ from 'lodash';
import * as ActionType from './actionTypes';
import * as api from '../../services';
import http from "../../services/http";
import Api from "../../api";
import {createActions} from "redux-actions";

export const closeLoginScreen = () => {
  NavigationActions.navigate({
    routeName: 'main',
    transitionStyle: 'inverted',
  });
};

export const goToMain = () => NavigationActions.navigate({ routeName: 'main' });

export const goToLogin = () =>
  NavigationActions.navigate({ routeName: 'login' });

export const goToForgot = () =>
  NavigationActions.navigate({ routeName: 'forgot' });

// --------- Token -----------
const { getTokenRequest, getTokenSuccess, getTokenError } = createActions({
  'GET_TOKEN_REQUEST': () => { },
  'GET_TOKEN_SUCCESS': data => ({ data }),
  'GET_TOKEN_ERROR': error => ({ error }),
});

export const getFirstToken = () => dispatch => {
  dispatch(getTokenRequest());
  Api.User.getFirstToken()
    .then(response => {
      dispatch(goToLogin());
      http.setAuthorizationHeader(_.get(response, 'data.content.token', ''));
      dispatch(getTokenSuccess(response.data));
    })
    .catch(response => {
      dispatch(getTokenError(response.error));
    });
};

// Login

export const loginStart = () => ({
  type: ActionType.LOGIN_START,
});

export const loginSuccess = (data, isRemember, accountLogin) => ({
  type: ActionType.LOGIN_SUCCESS,
  data,
  isRemember,
  accountLogin,
});

export const loginError = error => ({
  type: ActionType.LOGIN_ERROR,
  error,
});

export const login = (company_id, employee_id, password, isRemember) => (
  dispatch,
  getState,
) => {
  dispatch(loginStart());
  const formData = new FormData();
  formData.append('company_id', company_id);
  formData.append('employee_id', employee_id);
  formData.append('service_id', 4);
  formData.append('password', password);

  const header = {
    'CSRF-Token': getState().auth.firstToken,
    'Content-Type': 'multipart/form-data',
    Accept: 'application/json',
  };

  api.clientAPI
    .post(api.API_LOGIN, formData, {
      headers: header,
    })
    .then(response => {
      const dataLogin = {
        company_id,
        employee_id,
        password,
        isRemember,
      };
      const accountLogin = JSON.stringify(dataLogin);
      dispatch(loginSuccess(response.data, isRemember, accountLogin));
      dispatch(goToMain());
    })
    .catch(error => {
      dispatch(loginError(error));
    });
};
