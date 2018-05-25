// @flow
import React, { Component } from 'react';
import { SafeAreaView } from 'react-native';
import { connect } from 'react-redux';
import { MaterialIndicator } from 'react-native-indicators';
import { styles } from './styles';
import { ColorPalette } from '../../../common/colors';

type Props = {
  children: any,
  isLoading: boolean,
  style: Object,
};

class LoadingContainerView extends Component<Props> {
  render() {
    return (
      <SafeAreaView style={this.props.style}>
        {this.props.children}
        {this.props.isLoading ? (
          <MaterialIndicator
            style={styles.activity_indicator}
            color={ColorPalette.blazeOrange}
          />
        ) : null}
      </SafeAreaView>
    );
  }
}

const mapStateToProps = () => ({});

const mapDispatchToProps = () => ({});

export const LoadingContainer = connect(mapStateToProps, mapDispatchToProps)(
  LoadingContainerView,
);
