// @flow
import { reducer as formReducer } from 'redux-form';
import { combineReducers } from 'redux';
import { navReducer } from '../navigation/reducer';
import authReducer from '../features/auth/reducer';
import { expenseReducer } from '../features/expense/reducer';

export default combineReducers({
  nav: navReducer,
  auth: authReducer,
  expense: expenseReducer,
  form: formReducer,
});
