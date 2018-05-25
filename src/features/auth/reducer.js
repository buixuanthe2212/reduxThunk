// @flow
// import { NavigationActions } from 'react-navigation';
import * as ActionType from './actionTypes';
import { configHeaderToken } from '../../services';
import {
  saveToken,
  saveRememberLogin,
  clearRememberLogin,
} from '../../common/AsyncStorage';

export default (
  state: Object = {
    isLogin: false,
    isLoadingSplash: false,
    firstToken: null,
    accessToken: null,
  },
  action: Object => Object,
) => {
  switch (action.type) {
    // Login
    case ActionType.LOGIN_START:
      return {
        ...state,
        isLoading: true,
      };
    case ActionType.LOGIN_SUCCESS:
      configHeaderToken(action.data.token);
      saveToken(action.data.token);

      if (action.isRemember) {
        saveRememberLogin(action.accountLogin);
      } else {
        clearRememberLogin();
      }

      return {
        ...state,
        isLogin: true,
        accessToken: action.data.token,
        isLoading: false,
      };
    case ActionType.LOGIN_ERROR:
      return {
        ...state,
        error: action.error,
      };

    // --- Token
    case "GET_TOKEN_REQUEST":
      return {
        ...state,
        isLoadingSplash: true,
      };

    case "GET_TOKEN_SUCCESS":
      return {
        ...state,
        firstToken: action.data.content.token,
        isLoadingSplash: false,
      };

    case "GET_TOKEN_ERROR":
      return {
        ...state,
        isLoadingSplash: false,
      };

    default:
      return state;
  }
};
