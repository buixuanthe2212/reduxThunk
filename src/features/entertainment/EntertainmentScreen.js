// @flow
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { View, Text } from 'react-native';
import { styles } from './styles';

class EntertainmentView extends Component {
  render() {
    return (
      <View style={styles.container}>
        <Text>交際費精算申請</Text>
      </View>
    );
  }
}

const mapStatesToProps = () => ({});

const mapDispatchToProps = () => ({});
export const EntertainmentScreen = connect(
  mapStatesToProps,
  mapDispatchToProps,
)(EntertainmentView);
