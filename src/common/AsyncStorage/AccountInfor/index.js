import { AsyncStorage } from 'react-native';

const KEY_REMEMBER_LOGIN = 'KEY_REMEMBER_LOGIN';

// Remember
export const saveRememberLogin = data => {
  AsyncStorage.setItem(KEY_REMEMBER_LOGIN, data);
};

export const getRememberLogin = () => {
  AsyncStorage.getItem(KEY_REMEMBER_LOGIN, (err, data) => {
    console.log('getRememberLogin: ', data);
  }) || null;
};

export const clearRememberLogin = () => {
  AsyncStorage.removeItem(KEY_REMEMBER_LOGIN) || null;
};
