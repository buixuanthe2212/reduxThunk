import React, { Component } from 'react';
import { connect } from 'react-redux';
import { View, ActivityIndicator } from 'react-native';
import { getFirstToken } from '../actions';
import { styles } from './styles';

class SplashView extends Component {
  static navigationOptions = {
    header: null,
  };
  componentDidMount() {
    this.props.callGetToken();
  }

  render() {
    return (
      <View style={styles.container}>
        {this.props.isLoadingSplash ? (
          <ActivityIndicator size="large" color="#0000ff" />
        ) : null}
      </View>
    );
  }
}

const mapStatesToProps = state => ({
  isLoading: state.auth.isLoading,
});

const mapDispatchToProps = dispatch => ({
  callGetToken: () => dispatch(getFirstToken()),
});
export const SplashScreen = connect(mapStatesToProps, mapDispatchToProps)(
  SplashView,
);
