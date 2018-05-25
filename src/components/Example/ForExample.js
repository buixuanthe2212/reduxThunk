// @flow
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { View, Text, TouchableOpacity } from 'react-native';

import { styles } from './styles';

class ForExample extends Component {
  constructor(props) {
    super(props)

    this.state = {
      stateExample: null,
    }
  }

  functionTouchableOpacity = () => {
    console.log('functionTouchableOpacity');
  }

  render() {
    return (
      <View style={styles.container}>
        <TouchableOpacity onPress={this.functionTouchableOpacity}>
        </TouchableOpacity>

        <Text>交際費精算申請</Text>
      </View>
    );
  }
}

const mapStatesToProps = () => ({});

const mapDispatchToProps = () => ({});

export const ForExample = connect(
  mapStatesToProps,
  mapDispatchToProps,
)(ForExample);
