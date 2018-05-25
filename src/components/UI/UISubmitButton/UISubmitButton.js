// @flow
import React, { PureComponent } from 'react';
import { TouchableOpacity, Text } from 'react-native';
import styles from './styles';

export class UISubmitButton extends PureComponent<{
  title: string,
  buttonStyle?: any,
  textStyle?: any,
  onPress?: () => any
}> {
  render() {
    return (
      <TouchableOpacity
        style={[styles.button, this.props.buttonStyle]}
        onPress={this.props.onPress}
      >
        <Text
          adjustsFontSizeToFit
          numberOfLines={1}
          style={[styles.text, this.props.textStyle]}
        >
          {this.props.title}
        </Text>
      </TouchableOpacity>
    );
  }
}
