// @flow
import { NavigationActions } from 'react-navigation';
import Api from '../../api';
import { createActions } from 'redux-actions';

import { convertParamsGet } from '../../helpers/Convert';

export const goToSearch = () =>
  NavigationActions.navigate({ routeName: 'createExpense' });

export const goToCreateExpense = () =>
  NavigationActions.navigate({ routeName: 'createExpense' });

// get expense list
const {
  getListExpenseRequest,
  getListExpenseSuccess,
  getListExpenseFail,
} = createActions({
  GET_LIST_EXPENSE_REQUEST: () => {},
  GET_LIST_EXPENSE_SUCCESS: data => ({ data }),
  GET_LIST_EXPENSE_FAIL: error => ({ error }),
});

export const getExpenseList = paramsExpense => dispatch => {
  dispatch(getListExpenseRequest());

  const params = convertParamsGet(paramsExpense);

  return Api.User.getExpenseList(params)
    .then(({ data }) => {
      dispatch(getListExpenseSuccess(data));
    })
    .catch(error => {
      dispatch(getListExpenseFail(error));
    });
};
