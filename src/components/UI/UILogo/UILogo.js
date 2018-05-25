// @flow
import React, { Component } from 'react';
import { Image } from 'react-native';
import { styles } from './styles';
import { ic_logo } from '../../../../assets';

export class UILogo extends Component<{
  icon: any,
  style: any,
  onPress: Function
}> {
  render() {
    return <Image source={ic_logo} style={[styles.icon, this.props.style]} />;
  }
}
