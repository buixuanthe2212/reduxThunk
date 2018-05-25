// @flow
import React, { Component } from 'react';
import { Text } from 'react-native';
import { styles } from './styles';

export class UIText extends Component<{
  style?: any,
  text: string,
  textStyle?: any
}> {
  render() {
    return (
      <Text style={[styles.text, this.props.textStyle]}>{this.props.text}</Text>
    );
  }
}
