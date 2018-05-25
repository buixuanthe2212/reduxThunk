import http from '../services/http';

export default class User {
  /**
   * get expense list
   * @param: page=1&start_date=2018/05/31
   */
  static getExpenseList(params) {
    return http.get(`/v1/expenses${params}`);
  }

  /**
   * get token login
   */
  static getFirstToken() {
    return http.get('/v1/get_token');
  }
}
