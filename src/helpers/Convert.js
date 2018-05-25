import _ from 'lodash';

export const convertParamsGet = (params) => {
  if (_.isEmpty(params)) return ''
  const paramPage = `?page=${params.page}`
  const paramPerPage = `&perPage=${params.perPage}`
  let paramsReturn
  if (params.page) {
    paramsReturn = paramPage
    if (params.perPage) {
      paramsReturn = paramPage+paramPerPage
    }
  }
  return paramsReturn;
};