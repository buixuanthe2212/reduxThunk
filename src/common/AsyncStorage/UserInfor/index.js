import { AsyncStorage } from 'react-native';

const KEY_LOGIN_TOKEN = 'KEY_LOGIN_TOKEN';
const KEY_USER_INFOR = 'KEY_USER_INFOR';

// Token
export const saveToken = token => {
  AsyncStorage.setItem(KEY_LOGIN_TOKEN, token, err => {
    if (!err) {
      console.log(`Save: Key ${KEY_LOGIN_TOKEN} - Value ${token} success`);
    } else console.log('Save failed');
  });
};

export const getToken = () => {
  AsyncStorage.getItem(KEY_LOGIN_TOKEN, (err, result) => {
    console.log('result: ', result);
  }) || null;
};

export const clearToken = () => {
  AsyncStorage.removeItem(KEY_LOGIN_TOKEN, (err, result) => {
    console.log('Result removeItem: ', result);
  }) || null;
};

// User infor
export const saveUserInfor = data => {
  AsyncStorage.setItem(KEY_USER_INFOR, data, err => {
    if (!err) {
      console.log(`Save: Key ${KEY_USER_INFOR} - Value ${data} success`);
    } else console.log('Save failed');
  });
};

export const getUserInfor = () => {
  AsyncStorage.getItem(KEY_USER_INFOR, (err, result) => {
    console.log('result: ', result);
  }) || null;
};

export const clearUserInfor = () => {
  AsyncStorage.removeItem(KEY_USER_INFOR, (err, result) => {
    console.log('Result removeItem: ', result);
  }) || null;
};
