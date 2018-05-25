// @flow
import React, { Component } from 'react';
// import { Dispatch } from 'redux';
import { NavigationActions } from 'react-navigation';
import { connect } from 'react-redux';
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scrollview';
import { Platform, SafeAreaView } from 'react-native';
import { styles } from './styles';
import { ProfileInfo } from './ProfileInfo';
import {
  ic_history,
  ic_home,
  ic_setting,
  ic_help,
  ic_logout,
} from '../../../assets';
import { ItemMenu } from './ItemMenu';
import {
  goToHelpScreen,
  goToHistoryScreen,
  goToHomeScreen,
  goToSettingScreen,
  logout,
} from './actions';

type Props = {
  goToHelpScreen: Function,
  goToHistoryScreen: Function,
  goToSettingScreen: Function,
  goToHomeScreen: Function,
  logout: Function
};

class MenuView extends Component<Props> {
  onClickItem = () => {};

  navigateToScreen = route => () => {
    const navigateAction = NavigationActions.navigate({
      routeName: route,
    });
    this.props.navigation.dispatch(navigateAction);
  };

  render() {
    return (
      <SafeAreaView style={styles.container}>
        <ProfileInfo />
        <KeyboardAwareScrollView>
          <ItemMenu
            icon={ic_home}
            title="経費精算"
            onPress={() => {
              this.props.navigation.navigate('expense');
            }}
          />
          <ItemMenu
            icon={ic_history}
            title="交際費精算申請"
            onPress={() => {
              this.props.navigation.navigate('applyExpense');
            }}
          />
          <ItemMenu
            icon={ic_setting}
            title="仮払い申請"
            onPress={() => {
              this.props.navigation.navigate('applyPayment');
            }}
          />
          {Platform.OS === 'ios' ? null : (
            <ItemMenu
              icon={ic_help}
              title="IC-Card"
              onPress={() => {
                this.props.navigation.navigate('card');
              }}
            />
          )}
          <ItemMenu
            icon={ic_logout}
            title="Logout"
            onPress={() => {
              this.props.logout();
            }}
          />
        </KeyboardAwareScrollView>
      </SafeAreaView>
    );
  }
}

const mapStateToProps = () => ({});

const mapDispatchToProps = dispatch => ({
  goToHelpScreen() {
    dispatch(goToHelpScreen());
  },
  goToHistoryScreen() {
    dispatch(goToHistoryScreen());
  },
  goToSettingScreen() {
    dispatch(goToSettingScreen());
  },
  goToHomeScreen() {
    dispatch(goToHomeScreen());
  },
  logout() {
    dispatch(logout());
  },
});

export const MenuScreen = connect(mapStateToProps, mapDispatchToProps)(
  MenuView,
);
