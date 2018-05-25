// @flow
import React, { Component } from 'react';
import { Text, Image, TouchableOpacity, SafeAreaView } from 'react-native';
import { connect } from 'react-redux';
import { styles } from './styles';
import { ic_back } from '../../../../assets';

type Props = {
  title: string,
  navigation: any,
  onClickBack: any,
  onPress: any
};
type State = {};

class ToolbarView extends Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {};
  }

  render() {
    return (
      <SafeAreaView style={styles.toolbar}>
        <TouchableOpacity onPress={this.props.onPress}>
          <Image style={styles.back} source={ic_back} />
        </TouchableOpacity>
        <Text style={styles.title}>{this.props.title}</Text>
      </SafeAreaView>
    );
  }
}

const mapStatesToProps = () => ({});

const mapDispatchToProps = () => ({});

export const Toolbar = connect(mapStatesToProps, mapDispatchToProps)(
  ToolbarView,
);
