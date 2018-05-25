// @flow
import React, { Component } from 'react';
import { NavigationActions } from 'react-navigation';
import { connect } from 'react-redux';
import { View, Text } from 'react-native';
import { styles } from './styles';
import { Toolbar } from '../../../components/UI';

type Props = {
  getExpense: Function,
};
type State = {};

class CreateExpenseView extends Component<Props, State> {
  static navigationOptions = {
    header: null,
  };
  render() {
    return (
      <View>
        <Toolbar
          title="Create expense"
          onPress={() =>
            this.props.navigation.dispatch(NavigationActions.back())
          }
        />
        <View style={styles.container}>
          <Text style={{ marginTop: 100, justifyContent: 'center' }}>
            Create Expense
          </Text>
        </View>
      </View>
    );
  }
}

const mapStatesToProps = () => ({});

const mapDispatchToProps = () => ({});
export const CreateExpenseScreen = connect(
  mapStatesToProps,
  mapDispatchToProps,
)(CreateExpenseView);
