// @flow
import React, { Component } from 'react';
import { MaterialIndicator } from 'react-native-indicators';
import { connect } from 'react-redux';
import { View, FlatList, Text } from 'react-native';
import PropTypes from 'prop-types';
import _ from 'lodash';

import { styles } from './styles';
import { UIButton } from '../../../components/UI';
import { getExpenseList, goToCreateExpense } from '../action';
import { ItemExpense } from './ItemExpense';
import { ColorPalette } from '../../../common/colors';
import { ITEM_PER_PAGE } from '../../../common/constants';
import { strings } from '../../../../locales/i18n';

class ExpenseView extends Component<Props, State> {

  static propTypes = {
    getExpenseList: PropTypes.func.isRequired,
  };

  constructor(props) {
    super(props);
    this.state = {
      refresh: false,
    }
  }

  componentDidMount() {
    this.props.getExpenseList({ page: 1, perPage: 5 });
  }

  handleRefresh = () => {
    this.props.getExpenseList({ page: 1, perPage: ITEM_PER_PAGE });
    this.setState({ refresh: false });
  };

  _renderItem = (item, index) => {
    return (
      <ItemExpense index={index} item={item} />
    );
  }

  _renderEmptyView = (requesting) => (
    <View style={styles.empty_view}>
      {!requesting && <Text>Expense Empty</Text>}
    </View>
  );

  _renderFooter(requesting) {
    if (!requesting) return
    return (
      <View
        style={{
          paddingVertical: 20,
          borderTopWidth: 1,
          borderColor: ColorPalette.white,
        }}
      >
        <MaterialIndicator color={ColorPalette.baseColor} />
      </View>
    );
  };

  _keyExtractor = (item, index) => index;

  onEndReached = (expenseLists) => {
    if (_.isEmpty(this.props.expenseLists.error)) {
      const { getExpenseList } = this.props;
      if (_.get(expenseLists, 'requesting', false) || !_.get(expenseLists, 'canLoadMore', false)) return;
      const page = _.get(expenseLists, 'result.content.cur_page', 1) + 1;
      getExpenseList({ page, perPage: 5 });
    }
  }

  render() {
    const { expenseLists } = this.props;
    const expenseList = _.get(expenseLists, 'result.content.data');
    const isLoading = _.get(expenseLists, 'requesting') && _.get(expenseLists, 'result') === null;
    return (
      <View style={{ flex: 1 }}>

        <UIButton
          buttonStyle={styles.buttonCreateNew}
          textStyle={styles.textCreateNew}
          title={strings('button.createNew')}
          onPress={() => {
            this.props.goToCreateExpense();
          }}
        />

        <UIButton
          buttonStyle={styles.buttonCreateNew}
          textStyle={styles.textCreateNew}
          title={strings('button.search')}
          onPress={() => {}}
        />

        {isLoading ? (
          <View style={{ flex: 1, alignItems: 'center' }}>
            <MaterialIndicator color={ColorPalette.baseColor} />
          </View>
        ) : (
          <View style={{ flex: 1 }}>
            <FlatList
              data={expenseList}
              renderItem={item => this._renderItem(item.item, item.index)}
              ListEmptyComponent={() => this._renderEmptyView(_.get(expenseLists, 'requesting'))}
              ListFooterComponent={this._renderFooter(_.get(expenseLists, 'requesting'))}
              keyExtractor={this._keyExtractor}
              onRefresh={() => this.handleRefresh()}
              refreshing={this.state.refresh}
              onEndReached={() => this.onEndReached(expenseLists)}
              onEndReachedThreshold={0.2}
            />
          </View>
        )}
      </View>
    );
  }
}

const mapStatesToProps = state => ({
  expenseLists: state.expense.expenseLists,
});

const mapDispatchToProps = dispatch => ({
  getExpenseList: (params) => dispatch(getExpenseList(params)),
  goToCreateExpense: () => dispatch(goToCreateExpense()),
});

export const ExpenseScreen = connect(mapStatesToProps, mapDispatchToProps)(
  ExpenseView,
);
