import { handleActions } from 'redux-actions';
import _ from 'lodash';

const initialState = {
  expenseLists: {
    requesting: false,
    result: null,
    error: null,
    status: '',
    canLoadMore: false
  },
};

export const expenseReducer = handleActions({
  'GET_LIST_EXPENSE_REQUEST': (state, { payload }) => {
    return ({
      ...state,
      expenseLists: {
        ...initialState.expenseLists,
        requesting: true,
        status: '',
        error: null,
        result: state.expenseLists.result || null,
      }
    })
  },
  'GET_LIST_EXPENSE_SUCCESS': (state, { payload }) => {
    const canLoadMore = (_.get(payload, 'data.content.data.length') < _.get(payload, 'data.content.total'))
      && (_.get(payload, 'data.content.data.length') === _.get(payload, 'data.content.per_page'));
    let resultLoadmore = {}
    if (state.expenseLists.result) {
      resultLoadmore = {
        ...state.expenseLists.result,
        content: {
          ...state.expenseLists.result.content,
          per_page: payload.data.content.per_page,
          cur_page: payload.data.content.cur_page,
          data: [...state.expenseLists.result.content.data, ...payload.data.content.data],
        },
      };
    }

    return ({
      ...state,
      expenseLists: {
        ...state.expenseLists,
        requesting: false,
        status: 'success',
        result: _.get(payload, 'data.content.cur_page') > 1
          ? resultLoadmore
          : payload.data,
        canLoadMore,
      },
    });
  },
  'GET_LIST_EXPENSE_FAIL': (state, { payload }) => {
    return ({
      ...state,
      expenseLists: {
        ...state.expenseLists,
        requesting: false,
        status: 'error',
        error: payload.error
      }
    })
  },
}, initialState);

export default expenseReducer;